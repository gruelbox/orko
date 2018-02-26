package com.grahamcrockford.oco.core.spi;

public interface JobProcessor<T extends Job> {

  public boolean start();

  public void stop();

  public interface Factory<T extends Job> {
    public JobProcessor<T> create(T job, JobControl jobControl);
  }

}