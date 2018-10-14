package com.grahamcrockford.oco.spi;

import com.grahamcrockford.oco.notification.Status;

public interface JobControl {

  public void replace(Job job);

  public void finish(Status status);

}
