package com.grahamcrockford.oco.core.spi;

public interface JobControl {

  public void replace(Job job);

  public void finish();

}
