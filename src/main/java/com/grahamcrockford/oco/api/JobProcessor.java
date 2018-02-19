package com.grahamcrockford.oco.api;

public interface JobProcessor<T extends Job> {

  public java.util.Optional<T> process(T order) throws InterruptedException;

}
