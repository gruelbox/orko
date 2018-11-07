package com.grahamcrockford.orko.guardian;

final class KeepAliveEvent {

  public static final KeepAliveEvent INSTANCE = new KeepAliveEvent();

  private KeepAliveEvent() {}

}
