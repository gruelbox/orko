package com.gruelbox.orko.submit;

import com.gruelbox.orko.spi.Job;

/**
 * Submits new jobs.
 */
public interface JobSubmitter {

  /**
   * Submits a job.
   *
   * @param job The job.
   * @return The job including any generated id.
   * @throws Exception Exception.
   */
  Job submitNew(Job job) throws Exception;

  /**
   * As {@link #submitNew(Job)} but restates any
   * checked exceptions as {@link RuntimeException}.
   *
   * @param job he job.
   * @return The job including any generated id.
   */
  default Job submitNewUnchecked(Job job) {
    try {
      return submitNew(job);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}