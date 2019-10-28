package com.gruelbox.orko.exchange;

/**
 * For testing. Fires signals at key events allowing tests to orchestrate.
 */
interface LifecycleListener {
  default void onBlocked(String exchange) {}
  default void onStop(String exchange) {}
  default void onStopMain() {}
}
