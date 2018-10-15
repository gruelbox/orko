package com.grahamcrockford.orko.guardian;

import java.util.UUID;

import com.google.inject.Inject;
import com.grahamcrockford.orko.spi.Job;
import com.grahamcrockford.orko.submit.JobSubmitter;

/**
 * Implementation of {@link JobSubmitter} which runs the job within the same process.
 */
public class InProcessJobSubmitter implements JobSubmitter {

  private final JobRunner jobRunner;

  @Inject
  InProcessJobSubmitter(JobRunner jobRunner) {
    this.jobRunner = jobRunner;
  }

  @Override
  public Job submitNew(Job job) throws Exception {
    Job result = job.toBuilder().id(UUID.randomUUID().toString()).build();
    if (!jobRunner.runNew(result, () -> {}, () -> {})) {
      throw new JobNotUniqueException();
    }
    return result;
  }


  public final class JobNotUniqueException extends Exception {
    private static final long serialVersionUID = 7113718773155036498L;
    JobNotUniqueException() {
      super("Job cannot be locked. Already exists or UUID re-used");
    }
  }
}