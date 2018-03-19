package com.grahamcrockford.oco.spi;

public final class KeepAliveEvent {

  public static final KeepAliveEvent INSTANCE = new KeepAliveEvent();

  private KeepAliveEvent() {}

}
