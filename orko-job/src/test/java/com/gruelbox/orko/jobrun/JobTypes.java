package com.gruelbox.orko.jobrun;

import com.google.common.collect.ImmutableList;
import com.gruelbox.orko.jobrun.spi.Job;
import com.gruelbox.orko.jobrun.spi.JobTypeContribution;

public class JobTypes implements JobTypeContribution {

  @Override
  public Iterable<Class<? extends Job>> jobTypes() {
    return ImmutableList.of(
      AutoValue_DummyJob.class,
      AutoValue_TestingJob.class,
      AutoValue_AsynchronouslySelfStoppingJob.class
    );
  }
}