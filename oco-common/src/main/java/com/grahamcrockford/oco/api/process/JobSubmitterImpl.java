package com.grahamcrockford.oco.api.process;

import java.util.UUID;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.grahamcrockford.oco.mq.JobPublisher;
import com.grahamcrockford.oco.mq.JobPublisher.PublishFailedException;
import com.grahamcrockford.oco.spi.Job;

@Singleton
class JobSubmitterImpl implements JobSubmitter {

  private final JobPublisher announcer;

  @Inject
  JobSubmitterImpl(JobPublisher announcer) {
    this.announcer = announcer;
  }

  /**
   * @throws PublishFailedException
   * @see com.grahamcrockford.oco.api.process.JobSubmitter#submitNew(T)
   */
  @Override
  @SuppressWarnings({ "unchecked" })
  public <T extends Job> T submitNew(T job) throws PublishFailedException {
    T result = (T) job.toBuilder().id(UUID.randomUUID().toString()).build();
    announcer.publishJob(result);
    return result;
  }
}