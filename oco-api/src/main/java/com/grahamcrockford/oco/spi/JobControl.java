package com.grahamcrockford.oco.spi;

public interface JobControl {

  public void replace(Job job);

  public void finish();

}
