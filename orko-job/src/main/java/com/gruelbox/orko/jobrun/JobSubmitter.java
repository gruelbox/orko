package com.gruelbox.orko.jobrun;

import com.gruelbox.orko.jobrun.spi.Job;

/**
 * Submits new jobs.
 */
public interface JobSubmitter {

  /**
   * Submits a job.
   *
   * <p>
   * {@link Job#id()} may be set or not. If set, the value passed will be retained
   * so can be used to ensure idempotency. If not set, it will be set to a random
   * value before saving.
   * </p>
   *
   * @param job The job.
   * @return The job including any generated id. Do not assume this will be the
   *         same object as first supplied.
   * @throws Exception Exception.
   */
  Job submitNew(Job job) throws Exception;

  /**
   * As {@link #submitNew(Job)} but restates any checked exceptions as
   * {@link RuntimeException}.
   *
   * <p>
   * {@link Job#id()} may be set or not. If set, the value passed will be retained
   * so can be used to ensure idempotency. If not set, it will be set to a random
   * value before saving.
   * </p>
   *
   * @param job The job.
   * @return The job including any generated id. Do not assume this will be the
   *         same object as first supplied.
   */
  default Job submitNewUnchecked(Job job) {
    try {
      return submitNew(job);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}