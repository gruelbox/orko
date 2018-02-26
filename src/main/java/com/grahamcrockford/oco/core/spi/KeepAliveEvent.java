package com.grahamcrockford.oco.core.spi;

public final class KeepAliveEvent {

  public static final KeepAliveEvent INSTANCE = new KeepAliveEvent();

  private KeepAliveEvent() {}

}
