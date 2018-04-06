package com.grahamcrockford.oco.api.process;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.grahamcrockford.oco.api.mq.JobPublisher;
import com.grahamcrockford.oco.spi.Job;

@Singleton
class JobSubmitterImpl implements JobSubmitter {

  private final JobAccess advancedOrderAccess;
  private final JobPublisher announcer;
  @Inject
  JobSubmitterImpl(JobAccess jobAccess, JobPublisher announcer) {
    this.advancedOrderAccess = jobAccess;
    this.announcer = announcer;
  }

  /**
   * @see com.grahamcrockford.oco.api.process.JobSubmitter#submitNew(T)
   */
  @Override
  @SuppressWarnings({ "unchecked" })
  public <T extends Job> T submitNew(T job) {
    T result = (T) advancedOrderAccess.insert(job, Job.class);
    announcer.publishJob(result);
    return result;
  }
}