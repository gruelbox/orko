package com.grahamcrockford.orko.spi;

import com.grahamcrockford.orko.notification.Status;

public interface JobControl {

  public void replace(Job job);

  public void finish(Status status);

}
