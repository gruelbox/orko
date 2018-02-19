package com.grahamcrockford.oco.api;

public interface JobBuilder {

  public JobBuilder id(String id);

  public Job build();

}
