package com.grahamcrockford.oco.api;

public interface AdvancedOrderProcessor<T extends AdvancedOrder> {

  public java.util.Optional<T> process(T order) throws InterruptedException;

}
