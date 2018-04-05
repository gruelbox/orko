package com.grahamcrockford.oco.api.process;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.grahamcrockford.oco.api.mq.JobAnnouncer;
import com.grahamcrockford.oco.spi.Job;

@Singleton
class JobSubmitterImpl implements JobSubmitter {

  private final JobAccess advancedOrderAccess;
  private final JobAnnouncer announcer;
  @Inject
  JobSubmitterImpl(JobAccess jobAccess, JobAnnouncer announcer) {
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
    announcer.announceJob(result);
    return result;
  }
}