package com.gruelbox.orko.job;

import java.util.ServiceLoader;

import com.google.common.collect.ImmutableList;
import com.gruelbox.orko.jobrun.spi.Job;
import com.gruelbox.orko.jobrun.spi.JobTypeContribution;

/**
 * Orko jobs (registered by {@link ServiceLoader}).
 *
 * @author Graham Crockford
 */
public class JobTypes implements JobTypeContribution {
  @Override
  public Iterable<Class<? extends Job>> jobTypes() {
    return ImmutableList.of(
      AutoValue_Alert.class,
      AutoValue_LimitOrderJob.class,
      OneCancelsOther.class,
      SoftTrailingStop.class,
      StatusUpdateJob.class
    );
  }
}