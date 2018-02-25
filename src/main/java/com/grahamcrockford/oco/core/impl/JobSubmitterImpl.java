package com.grahamcrockford.oco.core.impl;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.google.inject.Inject;
import com.grahamcrockford.oco.core.api.JobSubmitter;
import com.grahamcrockford.oco.core.impl.JobExecutor.Factory;
import com.grahamcrockford.oco.core.spi.Job;

class JobSubmitterImpl implements JobSubmitter {

  private final JobAccess advancedOrderAccess;
  private final JobLocker jobLocker;
  private final ExecutorService executorService;
  private final Factory jobExecutorFactory;
  private final UUID uuid;

  @Inject
  JobSubmitterImpl(JobAccess advancedOrderAccess, JobLocker jobLocker, JobExecutor.Factory jobExecutorFactory) {
    this.advancedOrderAccess = advancedOrderAccess;
    this.jobLocker = jobLocker;
    this.jobExecutorFactory = jobExecutorFactory;
    this.executorService = Executors.newCachedThreadPool();
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
      executorService.execute(jobExecutorFactory.create(job, uuid));
      return true;
    } else {
      return false;
    }
  }

  /**
   * @see com.grahamcrockford.oco.core.api.JobSubmitter#shutdown()
   */
  @Override
  public void shutdown() throws InterruptedException {
    executorService.shutdownNow();
    executorService.awaitTermination(30, TimeUnit.SECONDS);
  }
}