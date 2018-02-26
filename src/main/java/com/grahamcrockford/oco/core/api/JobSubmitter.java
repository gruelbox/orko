package com.grahamcrockford.oco.core.api;

import com.grahamcrockford.oco.core.spi.Job;

public interface JobSubmitter {

  <T extends Job> T submitNew(T job);

  boolean submitExisting(Job job);

}