package com.gruelbox.orko.jobrun.spi;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;

/**
 * A job may start multiple times, but only run exactly once to completion. It
 * can be serialized, and thus is persistent until finishing. Its behaviour
 * must be idempotent to avoid double-processing.
 *
 * <p>
 * Instances of this class are immutable, but can be copied and modified (using
 * {@link #toBuilder()}) and then saved, replacing the existing job.
 * </p>
 *
 * <p>
 * The processing code is kept separate from the data.  The applicatiom will
 * inject an instance of {@link #processorFactory()} to create the processor
 * necessary to run the job.
 * </p>
 *
 * <p>
 * In a multi instance application, only one instance can lock and run the job
 * at a time. In the event of a failure of the instance holding the lock, the
 * lock will time out and get picked up by another instance.
 * </p>
 *
 * <p>
 * Jobs are assumed to be asynchronous. They should return immediately on
 * startup and either subscribe to the necessary callbacks or spawn background
 * threads to achieve continuous processing.
 * </p>
 *
 * @author Graham Crockford
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.CUSTOM, include=JsonTypeInfo.As.PROPERTY, property="jobType")
@JsonTypeIdResolver(JobTypeResolver.class)
public interface Job {

  /**
   * @return The job id.
   */
  String id();

  /**
   * Creates a duplicate copy of the job as a builder, allowing a modified
   * version to be created.
   *
   * <p>Subclasses should overload the return type of this method to match
   * the concrete job type.</p>
   *
   * @return A populated builder.
   */
  JobBuilder<? extends Job> toBuilder();

  /**
   * The class of the factory from which instances of the processor
   * class can be created.
   *
   * @return The processor factory class.
   */
  Class<? extends JobProcessor.Factory<? extends Job>> processorFactory();

}