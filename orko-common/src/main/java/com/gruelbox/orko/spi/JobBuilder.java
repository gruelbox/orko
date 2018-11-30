package com.gruelbox.orko.spi;

public interface JobBuilder<T extends Job> {

  public JobBuilder<T> id(String id);

  public T build();

}
