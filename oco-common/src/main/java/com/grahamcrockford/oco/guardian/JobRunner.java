package com.grahamcrockford.oco.guardian;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.grahamcrockford.oco.spi.Job;
import com.grahamcrockford.oco.spi.JobControl;
import com.grahamcrockford.oco.spi.JobProcessor;
import com.grahamcrockford.oco.spi.KeepAliveEvent;
import com.grahamcrockford.oco.submit.JobAccess;
import com.grahamcrockford.oco.submit.JobLocker;
import com.grahamcrockford.oco.submit.JobAccess.JobAlreadyExistsException;

@Singleton
class JobRunner {

  private static final Logger LOGGER = LoggerFactory.getLogger(JobRunner.class);

  private final JobAccess jobAccess;
  private final JobLocker jobLocker;
  private final UUID uuid;
  private final Injector injector;
  private final AsyncEventBus asyncEventBus;

  @Inject
  JobRunner(JobAccess advancedOrderAccess, JobLocker jobLocker, Injector injector, AsyncEventBus asyncEventBus) {
    this.jobAccess = advancedOrderAccess;
    this.jobLocker = jobLocker;
    this.injector = injector;
    this.asyncEventBus = asyncEventBus;
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
      if (!status.compareAndSet(JobStatus.CREATED, JobStatus.STARTING))
        throw new IllegalStateException("Job lifecycle status indicates re-use of lifetime manager: " + job);
      LOGGER.info(job + " starting...");
      if (!processor.start()) {
        jobAccess.delete(job.id());
        processor.stop();
        status.set(JobStatus.STOPPED);
        LOGGER.debug(job + " finished immediately");
      } else {
        status.set(JobStatus.RUNNING);
        asyncEventBus.register(this);
        LOGGER.debug(job + " started");
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
      LOGGER.info(job + " stopping due to shutdown");
      if (!stopAndUnregister()) {
        LOGGER.debug("Stop of job which is already shutting down: " + job);
        return;
      }
      jobLocker.releaseLock(job.id(), uuid);
      LOGGER.debug(job + " stopped due to shutdown");
    }

    @Override
    public void replace(Job newVersion) {
      LOGGER.debug(job + " replacing...");
      if (!stopAndUnregister()) {
        LOGGER.debug("Replacement of job which is already shutting down: " + job);
        return;
      }
      jobAccess.update(newVersion);
      new JobLifetimeManager(newVersion).start();
      LOGGER.debug(newVersion + " replaced");
    }

    @Override
    public void finish() {
      LOGGER.debug(job + " finishing...");
      if (!stopAndUnregister()) {
        LOGGER.debug("Finish of job which is already shutting down: " + job);
        return;
      }
      jobAccess.delete(job.id());
      LOGGER.info(job + " finished");
    }

    private boolean stopAndUnregister() {
      if (!status.compareAndSet(JobStatus.RUNNING, JobStatus.STOPPING))
        return false;
      processor.stop();
      asyncEventBus.unregister(this);
      status.set(JobStatus.STOPPED);
      return true;
    }

    @Override
    public String toString() {
      return "JobSubmitter[" + job + "]";
    }
  }
}