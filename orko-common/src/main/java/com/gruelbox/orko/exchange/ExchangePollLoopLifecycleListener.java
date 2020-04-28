package com.gruelbox.orko.exchange;

public interface ExchangePollLoopLifecycleListener {

  default void onBlocked() {}

  default void onStop() {}

}
