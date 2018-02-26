package com.grahamcrockford.oco.core.impl;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.grahamcrockford.oco.core.api.JobSubmitter;
import com.grahamcrockford.oco.core.spi.Job;
import com.grahamcrockford.oco.core.spi.JobControl;
import com.grahamcrockford.oco.core.spi.JobProcessor;
import com.grahamcrockford.oco.core.spi.KeepAliveEvent;

@Singleton
class JobSubmitterImpl implements JobSubmitter {

  private static final Logger LOGGER = LoggerFactory.getLogger(JobSubmitterImpl.class);

  private final JobAccess advancedOrderAccess;
  private final JobLocker jobLocker;
  private final UUID uuid;
  private final Injector injector;
  private final AsyncEventBus asyncEventBus;

  @Inject
  JobSubmitterImpl(JobAccess advancedOrderAccess, JobLocker jobLocker, Injector injector, AsyncEventBus asyncEventBus) {
    this.advancedOrderAccess = advancedOrderAccess;
    this.jobLocker = jobLocker;
    this.injector = injector;
    this.asyncEventBus = asyncEventBus;
    this.uuid = UUID.randomUUID();
  }

  /**
   * @see com.grahamcrockford.oco.core.api.JobSubmitter#submitNew(T)
   */
  @Override
  @SuppressWarnings({ "unchecked" })
  public <T extends Job> T submitNew(T job) {
    job = (T) advancedOrderAccess.insert(job, Job.class);
    submitExisting(job);
    return job;
  }

  /**
   * @see com.grahamcrockford.oco.core.api.JobSubmitter#submitExisting(com.grahamcrockford.oco.core.spi.Job)
   */
  @Override
  public boolean submitExisting(Job job) {
    if (jobLocker.attemptLock(job.id(), uuid)) {
      job = advancedOrderAccess.load(job.id());
      new JobLifetimeManager(job).start();
      return true;
    } else {
      return false;
    }
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
      LOGGER.debug(job + " starting...");
      if (!processor.start()) {
        processor.stop();
        advancedOrderAccess.delete(job.id());
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
        if (stopAndUnregister())
          LOGGER.info(job + " stopped due to loss of lock");
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
      LOGGER.info(job + " stopped due to shutdown");
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void replace(Job newVersion) {
      LOGGER.debug(job + " replacing...");
      if (!stopAndUnregister()) {
        LOGGER.debug("Replacement of job which is already shutting down: " + job);
        return;
      }
      advancedOrderAccess.update(newVersion, (Class) newVersion.getClass());
      asyncEventBus.register(new JobLifetimeManager(newVersion));
      LOGGER.debug(newVersion + " replaced");
    }

    @Override
    public void finish() {
      LOGGER.debug(job + " finishing...");
      if (!stopAndUnregister()) {
        LOGGER.debug("Finish of job which is already shutting down: " + job);
        return;
      }
      advancedOrderAccess.delete(job.id());
      LOGGER.info(job + " finished");
    }

    private boolean stopAndUnregister() {
      if (!status.compareAndSet(JobStatus.RUNNING, JobStatus.STOPPING))
        return false;
      LOGGER.debug(job + " stopping...");
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