package com.gruelbox.orko.spi;

import com.gruelbox.orko.notification.Status;

public interface JobControl {

  public void replace(Job job);

  public void finish(Status status);

}
