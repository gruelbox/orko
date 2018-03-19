package com.grahamcrockford.oco.api.process;

import com.google.inject.ImplementedBy;
import com.grahamcrockford.oco.spi.Job;

@ImplementedBy(JobSubmitterImpl.class)
public interface JobSubmitter {

  <T extends Job> T submitNew(T job);

}