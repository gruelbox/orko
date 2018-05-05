package com.grahamcrockford.oco.submit;

import com.grahamcrockford.oco.spi.Job;

public interface JobSubmitter {

  Job submitNew(Job job) throws Exception;

  default Job submitNewUnchecked(Job job) {
    try {
      return submitNew(job);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}