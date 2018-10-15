package com.grahamcrockford.orko.guardian;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.grahamcrockford.orko.notification.Status;
import com.grahamcrockford.orko.notification.StatusUpdateService;
import com.grahamcrockford.orko.spi.Job;
import com.grahamcrockford.orko.spi.JobControl;
import com.grahamcrockford.orko.spi.JobProcessor;
import com.grahamcrockford.orko.spi.KeepAliveEvent;
import com.grahamcrockford.orko.submit.JobAccess;
import com.grahamcrockford.orko.submit.JobAccess.JobAlreadyExistsException;
import com.grahamcrockford.orko.submit.JobLocker;

@Singleton
class JobRunner {

  private static final Logger LOGGER = LoggerFactory.getLogger(JobRunner.class);

  private final JobAccess jobAccess;
  private final JobLocker jobLocker;
  private final UUID uuid;
  private final Injector injector;
  private final EventBus eventBus;
  private final StatusUpdateService statusUpdateService;

  @Inject
  JobRunner(JobAccess advancedOrderAccess, JobLocker jobLocker, Injector injector, EventBus eventBus, StatusUpdateService statusUpdateService) {
    this.jobAccess = advancedOrderAccess;
    this.jobLocker = jobLocker;
    this.injector = injector;
    this.eventBus = eventBus;
    this.statusUpdateService = statusUpdateService;
    this.uuid = UUID.randomUUID();
  }

  /**
   * Attempts to run a job that already exists.  Used by the poll loop.
   *
   * <p>Note that if the lock is successful, the job is only unlocked
   * on success or, if the job fails, due to the TTL removing it.
   * This creates an automatic delay on retries.</p>
   *
   * @param job The job.
   * @return True if the job could be locked and run successfully.
   */
  public boolean runExisting(Job job) {
    if (jobLocker.attemptLock(job.id(), uuid)) {
      job = jobAccess.load(job.id());
      new JobLifetimeManager(job).start();
      return true;
    } else {
      return false;
    }
  }

  /**
   * Attempts to insert and run a new job.
   *
   * <p>Given that inserting into the database guarantees that it will run at
   * some point, provides the ability to acknowledge this with a callback before
   * actually starting. This can be used to acknowledge the upstream
   * request.</p>
   *
   * <p>The request is ignored (and the callback called) if the job has already
   * been created, to avoid double-calling.</p>
   *
   * <p>Note that if the lock is successful, the job is only unlocked
   * on success or, if the job fails, due to the TTL removing it.
   * This creates an automatic delay on retries.</p>
   *
   * @param job The job.
   * @param ack The insertion callback.
   * @param reject If insertion failed
   * @throws Exception
   */
  public boolean runNew(Job job, ExceptionThrowingRunnable ack, ExceptionThrowingRunnable reject) throws Exception {

    boolean locked;
    try {
      locked = jobLocker.attemptLock(job.id(), uuid);
    } catch (Throwable t) {
      reject.run();
      LOGGER.warn("Job " + job.id() + " could not be locked. Request rejected.");
      throw t;
    }

    if (!locked) {
      return false;
    }

    try {
      jobAccess.insert(job);
    } catch (JobAlreadyExistsException e) {
      LOGGER.info("Job " + job.id() + " already exists. Request ignored.");
      ack.run();
      jobLocker.releaseLock(job.id(), uuid);
      return false;
    } catch (Throwable t) {
      reject.run();
      jobLocker.releaseLock(job.id(), uuid);
      throw t;
    }
    ack.run();
    new JobLifetimeManager(job).start();
    return true;
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

    private final Job job;
    private final JobProcessor<Job> processor;
    private final AtomicReference<JobStatus> status = new AtomicReference<>(JobStatus.CREATED);

    @SuppressWarnings("unchecked")
    JobLifetimeManager(Job job) {
      this.job = job;
      this.processor = ((JobProcessor.Factory<Job>) injector.getInstance(job.processorFactory())).create(job, this);
    }

    public void start() {
      start(false);
    }

    private void start(boolean replacement) {
      if (!status.compareAndSet(JobStatus.CREATED, JobStatus.STARTING))
        throw new IllegalStateException("Job lifecycle status indicates re-use of lifetime manager: " + job);
      if (!replacement)
        LOGGER.info(job + " starting...");
      Status result = safeStart();
      if (!replacement || !result.equals(Status.RUNNING))
        statusUpdateService.status(job.id(), result);
      if (result.equals(Status.SUCCESS) || result.equals(Status.FAILURE_PERMANENT)) {
        LOGGER.info(job + " finished immediately ({}), cleaning up", result);
        jobAccess.delete(job.id());  //  TODO think - what happens if something goes wrong here?
        safeStop();
        status.set(JobStatus.STOPPED);
        LOGGER.info(job + " cleaned up");
      } else if (result.equals(Status.FAILURE_TRANSIENT)) {
        LOGGER.info(job + " temporary failure. Sending back to queue for retry");
        processor.stop();
        status.set(JobStatus.STOPPED);
        LOGGER.info(job + " cleaned up");
      } else {
        register();
      }
    }

    @Subscribe
    public void onKeepAlive(KeepAliveEvent keepAlive) {
      if (!status.get().equals(JobStatus.RUNNING))
        return;
      if (!jobLocker.updateLock(job.id(), uuid)) {
        LOGGER.debug(job + " stopping due to loss of lock...");
        if (stopAndUnregister())
          LOGGER.debug(job + " stopped due to loss of lock");
      }
    }

    @Subscribe
    public void stop(StopEvent stop) {
      LOGGER.debug(job + " stopping due to shutdown");
      if (stopAndUnregister()) {
        jobLocker.releaseLock(job.id(), uuid);
        LOGGER.debug(job + " stopped due to shutdown");
      }
    }

    @Override
    public void replace(Job newVersion) {
      LOGGER.debug(job + " replacing...");
      if (!stopAndUnregister()) {
        LOGGER.debug("Replacement of job which is already shutting down: " + job);
        return;
      }
      jobAccess.update(newVersion);
      new JobLifetimeManager(newVersion).start(true);
      LOGGER.debug(newVersion + " replaced");
    }

    @Override
    public void finish(Status status) {
      LOGGER.info(job + " finishing ({})...", status);
      statusUpdateService.status(job.id(), status);
      if (!stopAndUnregister()) {
        LOGGER.warn("Finish of job which is already shutting down: " + job);
        return;
      }
      jobAccess.delete(job.id());
      LOGGER.info(job + " finished");
    }

    private synchronized void register() {
      if (status.compareAndSet(JobStatus.STARTING, JobStatus.RUNNING)) {
        status.set(JobStatus.RUNNING);
        eventBus.register(this);
        LOGGER.info(job + " started");
      }
    }

    private synchronized boolean stopAndUnregister() {
      if (status.compareAndSet(JobStatus.RUNNING, JobStatus.STOPPING)) {
        safeStop();
        eventBus.unregister(this);
        status.set(JobStatus.STOPPED);
        return true;
      } else if (status.compareAndSet(JobStatus.STARTING, JobStatus.STOPPED)) {
        return true;
      } else {
        LOGGER.debug("Stop of job which is already shutting down: " + job);
        return false;
      }
    }

    private Status safeStart() {
      Status result;
      try {
        result = processor.start();
      } catch (Throwable e) {
        LOGGER.error("Error in start() for job [{}].", e);
        result = Status.FAILURE_TRANSIENT;
      }
      return result;
    }

    private void safeStop() {
      try {
        processor.stop();
      } catch (Throwable e) {
        LOGGER.error("Error in stop() for job [{}]. Cleanup may not be complete.", e);
      }
    }

    @Override
    public String toString() {
      return "JobSubmitter[" + job + "]";
    }
  }
}