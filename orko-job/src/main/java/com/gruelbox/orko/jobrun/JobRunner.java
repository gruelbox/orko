/**
 * Orko - Copyright © 2018-2019 Graham Crockford
 *
 * <p>This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * <p>You should have received a copy of the GNU Affero General Public License along with this
 * program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.gruelbox.orko.jobrun;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.gruelbox.orko.db.Transactionally;
import com.gruelbox.orko.jobrun.JobAccess.JobAlreadyExistsException;
import com.gruelbox.orko.jobrun.spi.Job;
import com.gruelbox.orko.jobrun.spi.JobControl;
import com.gruelbox.orko.jobrun.spi.JobProcessor;
import com.gruelbox.orko.jobrun.spi.Status;
import com.gruelbox.orko.jobrun.spi.StatusUpdateService;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import org.hibernate.BaseSessionEventListener;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
class JobRunner {

  private static final Logger LOGGER = LoggerFactory.getLogger(JobRunner.class);

  private final JobAccess jobAccess;
  private final JobLocker jobLocker;
  private final UUID uuid;
  private final Injector injector;
  private final EventBus eventBus;
  private final StatusUpdateService statusUpdateService;
  private final Transactionally transactionally;
  private final ExecutorService executorService;
  private final Provider<SessionFactory> sessionFactory;

  @Inject
  JobRunner(
      JobAccess advancedOrderAccess,
      JobLocker jobLocker,
      Injector injector,
      EventBus eventBus,
      StatusUpdateService statusUpdateService,
      Transactionally transactionally,
      ExecutorService executorService,
      Provider<SessionFactory> sessionFactory) {
    jobAccess = advancedOrderAccess;
    this.jobLocker = jobLocker;
    this.injector = injector;
    this.eventBus = eventBus;
    this.statusUpdateService = statusUpdateService;
    this.transactionally = transactionally;
    this.executorService = executorService;
    this.sessionFactory = sessionFactory;
    uuid = UUID.randomUUID();
  }

  /**
   * Attempts to submit a job that already exists. Used by the poll loop.
   *
   * <p>Note that if the lock is successful, the job is only unlocked on success or, if the job
   * fails, due to the TTL removing it. This creates an automatic delay on retries.
   *
   * @param job The job.
   * @return True if the job could be locked and run successfully.
   */
  public boolean submitExisting(Job job) {
    if (jobLocker.attemptLock(job.id(), uuid)) {
      startAfterCommit(jobAccess.load(job.id()));
      return true;
    }
    return false;
  }

  /**
   * Attempts to insert and run a new job.
   *
   * <p>Given that inserting into the database guarantees that it will run at some point, provides
   * the ability to acknowledge this with a callback before actually starting. This can be used to
   * acknowledge the upstream request.
   *
   * <p>The request is ignored (and the callback called) if the job has already been created, to
   * avoid double-calling.
   *
   * <p>Note that if the lock is successful, the job is only unlocked on success or, if the job
   * fails, due to the TTL removing it. This creates an automatic delay on retries.
   *
   * @param job The job.
   * @param ack The insertion callback.
   * @param reject If insertion failed
   * @throws Exception If any errors occured.
   */
  public void submitNew(Job job, ExceptionThrowingRunnable ack, ExceptionThrowingRunnable reject)
      throws Exception {
    createJob(job, ack, reject);
    if (!attemptLock(job, reject)) {
      throw new RuntimeException("Created but could not immediately lock new job");
    }
    startAfterCommit(job);
  }

  private void startAfterCommit(Job job) {
    sessionFactory
        .get()
        .getCurrentSession()
        .addEventListeners(
            new BaseSessionEventListener() {
              private static final long serialVersionUID = 4340675209658497123L;

              @Override
              public void transactionCompletion(boolean successful) {
                if (successful) {
                  executorService.execute(() -> new JobLifetimeManager(job).start());
                }
              }
            });
  }

  private boolean attemptLock(Job job, ExceptionThrowingRunnable reject) throws Exception {
    boolean locked;
    try {
      locked = jobLocker.attemptLock(job.id(), uuid);
    } catch (Exception t) {
      reject.run();
      LOGGER.warn("Job {} could not be locked. Request rejected.", job.id());
      throw t;
    }
    return locked;
  }

  private void createJob(Job job, ExceptionThrowingRunnable ack, ExceptionThrowingRunnable reject)
      throws Exception {
    try {
      jobAccess.insert(job);
    } catch (JobAlreadyExistsException e) {
      LOGGER.info("Job {} already exists. Request ignored.", job.id());
      ack.run();
      throw e;
    } catch (Exception t) {
      reject.run();
      throw t;
    }
    ack.run();
  }

  public interface ExceptionThrowingRunnable {
    public void run() throws Exception;
  }

  private enum JobStatus {
    CREATED,
    STARTING,
    RUNNING,
    STOPPING,
    STOPPED
  }

  private final class JobLifetimeManager implements JobControl {

    private final JobProcessor<Job> processor;

    private volatile Job job;
    private volatile JobStatus status = JobStatus.CREATED;

    JobLifetimeManager(Job job) {
      this.job = job;
      processor = JobProcessor.createProcessor(job, this, injector);
    }

    private synchronized void start() {

      // Ensure this lifetime manager can only be used once
      if (!status.equals(JobStatus.CREATED))
        throw new IllegalStateException(
            "Job lifecycle status indicates re-use of lifetime manager: " + job);

      status = JobStatus.STARTING;
      LOGGER.info("{} starting...", job);

      // Attempt to run the startup method on the job and send a status update
      Status result = safeStart();
      statusUpdateService.status(job.id(), result);

      switch (result) {
        case FAILURE_PERMANENT:
        case SUCCESS:

          // If the job completed or failed permanently, delete it. The job
          // itself might be transactional if it's not working with external
          // resources, so allow to run in a nested transaction.
          LOGGER.debug("{} finished immediately ({}), cleaning up", job, result);
          transactionally.allowingNested().run(() -> jobAccess.delete(job.id()));
          safeStop();
          status = JobStatus.STOPPED;
          LOGGER.debug("{} cleaned up", job);
          break;

        case FAILURE_TRANSIENT:

          // Stop and let the job get picked up again. We don't release
          // the lock, instead let it expire naturally, giving us an
          // inherent retry delay
          LOGGER.warn("{}: temporary failure. Sending back to queue for retry", job);
          safeStop();
          LOGGER.debug("{} cleaned up", job);
          break;

        case RUNNING:

          // We are now running, so register for events
          register();
          break;

        default:
          throw new IllegalStateException("Unknown job status " + result);
      }
    }

    @Subscribe
    public synchronized void onKeepAlive(KeepAliveEvent keepAlive) {
      LOGGER.debug("{} checking lock...", job);
      if (!status.equals(JobStatus.RUNNING)) return;
      LOGGER.debug("{} updating lock...", job);
      if (!transactionally.call(() -> jobLocker.updateLock(job.id(), uuid))) {
        LOGGER.debug("{} stopping due to loss of lock...", job);
        if (stopAndUnregister()) LOGGER.debug("{} stopped due to loss of lock", job);
      }
    }

    @Subscribe
    public synchronized void stop(StopEvent stop) {
      LOGGER.debug("{} stopping due to shutdown", job);
      if (!stopAndUnregister()) {
        LOGGER.warn("Stop of job which is already shutting down. Status={}, job={}", status, job);
        return;
      }
      transactionally.allowingNested().run(() -> jobLocker.releaseLock(job.id(), uuid));
      LOGGER.debug("{} stopped due to shutdown", job);
    }

    @Override
    public synchronized void replace(Job newVersion) {
      Preconditions.checkNotNull(newVersion, "Job replaced with null");

      LOGGER.debug("{} replacing...", newVersion);
      if (!JobStatus.RUNNING.equals(status) && !JobStatus.STARTING.equals(status)) {
        LOGGER.warn(
            "Illegal state",
            new IllegalStateException(
                "Replacement of job which is already shutting down. Status="
                    + status
                    + ", job="
                    + newVersion));
        return;
      }

      // The job might be transactional, so participate if necessary
      transactionally.allowingNested().run(() -> jobAccess.update(newVersion));

      job = newVersion;
      processor.setReplacedJob(newVersion);

      LOGGER.debug("{} replaced", newVersion);
    }

    @Override
    public synchronized void finish(Status status) {
      Preconditions.checkArgument(
          status == Status.FAILURE_PERMANENT || status == Status.SUCCESS,
          "Finish condition must be success or permanent failure");

      LOGGER.info("{} finishing ({})...", job, status);
      statusUpdateService.status(job.id(), status);
      if (!stopAndUnregister()) {
        LOGGER.warn(
            "Finish of job which is already shutting down. Status={}, job={}", this.status, job);
        return;
      }
      // If this gets rolled back due to the job itself being transactional, that's
      // fine; we'll lose the lock anyway
      transactionally.allowingNested().run(() -> jobAccess.delete(job.id()));
      LOGGER.info("{} finished", job);
    }

    private void register() {
      if (!status.equals(JobStatus.STARTING)) {
        return;
      }
      status = JobStatus.RUNNING;
      eventBus.register(this);
      LOGGER.info("{} started", job);
    }

    private boolean stopAndUnregister() {
      if (status.equals(JobStatus.RUNNING)) {
        status = JobStatus.STOPPING;
        safeStop();
        eventBus.unregister(this);
        status = JobStatus.STOPPED;
        return true;
      } else if (status.equals(JobStatus.STARTING)) {
        status = JobStatus.STOPPED;
        return true;
      } else {
        return false;
      }
    }

    private Status safeStart() {
      Status result;
      try {
        result = processor.start();
      } catch (Exception e) {
        LOGGER.error("Error in start() for job [{}].", e);
        result = Status.FAILURE_TRANSIENT;
      }
      return result;
    }

    private void safeStop() {
      try {
        processor.stop();
      } catch (Exception e) {
        LOGGER.error("Error in stop() for job [{}]. Cleanup may not be complete.", e);
      }
    }

    @Override
    public String toString() {
      return "JobSubmitter[" + job + "]";
    }
  }
}
