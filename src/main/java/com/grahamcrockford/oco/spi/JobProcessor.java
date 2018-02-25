package com.grahamcrockford.oco.spi;

public interface JobProcessor<T extends Job> {

  public java.util.Optional<T> process(T order) throws InterruptedException;

}
