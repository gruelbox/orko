package com.grahamcrockford.oco.api.process;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.grahamcrockford.oco.spi.Job;

@Singleton
class JobSubmitterImpl implements JobSubmitter {

  private final JobAccess advancedOrderAccess;
  @Inject
  JobSubmitterImpl(JobAccess jobAccess) {
    this.advancedOrderAccess = jobAccess;
  }

  /**
   * @see com.grahamcrockford.oco.api.process.JobSubmitter#submitNew(T)
   */
  @Override
  @SuppressWarnings({ "unchecked" })
  public <T extends Job> T submitNew(T job) {
    return (T) advancedOrderAccess.insert(job, Job.class);
  }
}