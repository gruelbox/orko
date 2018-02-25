package com.grahamcrockford.oco.core.spi;

public interface JobBuilder<T extends Job> {

  public JobBuilder<T> id(String id);

  public T build();

}
