package com.gruelbox.orko.exchange;

import com.google.common.util.concurrent.RateLimiter;

public interface RateController {

  /** @see RateLimiter#acquire() */
  void acquire();

  /**
   * Cuts the throughput rate significantly on a temporary basis. This throttle will expire after a
   * period of time.
   */
  void throttle();

  /**
   * Slightly reduces the throughput permanently. Use on encountering rate limiting errors to reduce
   * the likelihood of hitting it again.
   */
  void backoff();

  /** Applies maximum throttling, effectively pausing. */
  void pause();

  boolean canThrottleFurther();
}
