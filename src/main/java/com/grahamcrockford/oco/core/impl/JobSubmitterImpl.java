package com.grahamcrockford.oco.core.impl;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.grahamcrockford.oco.core.api.JobSubmitter;
import com.grahamcrockford.oco.core.spi.Job;
import com.grahamcrockford.oco.core.spi.JobProcessor;

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
      LOGGER.info(job + " starting...");
      asyncEventBus.register(new JobLifetime(job));
      return true;
    } else {
      return false;
    }
  }

  private final class JobLifetime {

    private Job job;
    private final JobProcessor<Job> processor;

    @SuppressWarnings("unchecked")
    JobLifetime(Job job) {
      this.job = job;
      this.processor = (JobProcessor<Job>) injector.getInstance(job.processor());
      processor.start(job, this::onUpdated, this::onFinished);
      LOGGER.debug(job + " started");
    }

    @Subscribe
    public void onKeepAlive(KeepAliveEvent keepAlive) {
      if (!jobLocker.updateLock(job.id(), uuid)) {
        asyncEventBus.unregister(this);
        processor.stop(job);
        LOGGER.info(job + " stopped");
      }
    }

    @Subscribe
    public void stop(StopEvent stop) {
      LOGGER.debug(job + " stopping...");
      processor.stop(job);
      jobLocker.releaseLock(job.id(), uuid);
      LOGGER.info(job + " stopped");
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void onUpdated(Job newVersion) {
      LOGGER.debug("Saving updated job: " + newVersion);
      advancedOrderAccess.update(newVersion, (Class) newVersion.getClass());
      processor.stop(job);
      this.job = newVersion;
      processor.start(newVersion, this::onUpdated, this::onFinished);
    }

    public void onFinished() {
      LOGGER.info(job + " finished");
      advancedOrderAccess.delete(job.id());
    }
  }
}