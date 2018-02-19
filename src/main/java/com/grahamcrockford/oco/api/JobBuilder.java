package com.grahamcrockford.oco.api;

public interface JobBuilder<T extends Job> {

  public JobBuilder<T> id(String id);

  public T build();

}
