package com.gruelbox.orko.jobrun.spi;

/**
 * Builder for {@link Job}s.
 *
 * @author Graham Crockford
 * @param <T> The job type.
 */
public interface JobBuilder<T extends Job> {

  /**
   * Sets the job id.
   *
   * @param id The job id.
   * @return this, for method chaining.
   */
  public JobBuilder<T> id(String id);

  /**
   * Builds the completed job.
   *
   * @return The job.
   */
  public T build();

}
