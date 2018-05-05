package com.grahamcrockford.oco.submit;

import com.grahamcrockford.oco.spi.Job;

public interface JobSubmitter {

  <T extends Job> T submitNew(T job) throws Exception;

  default <T extends Job> T submitNewUnchecked(T job) {
    try {
      return submitNew(job);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}