/**
 * Orko
 * Copyright © 2018-2019 Graham Crockford
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.gruelbox.orko.exchange;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.RateLimiter;

/**
 * Wraps {@link RateLimiter} to perform exponential backoff.
 *
 * @author Graham Crockford
 */
public class RateController {

  private static final Logger LOGGER = LoggerFactory.getLogger(RateController.class);
  private static final int DEGREES_PERMITTED = 4;
  private static final int DIVISOR = 3;
  private final String exchangeName;
  private final RateLimiter rateLimiter;
  private final long throttleBy;
  private final double defaultRate;

  private volatile long throttleExpiryTime;
  private volatile int throttleLevel;

  /**
   * Constructor.
   *
   * @param exchangeName Exchange name.
   * @param rateLimiter The underlying rate limiter.
   * @param throttleBy The length of time a throttling should last.
   */
  RateController(String exchangeName, RateLimiter rateLimiter, Duration throttleBy) {
    this.exchangeName = exchangeName;
    this.rateLimiter = rateLimiter;
    this.throttleBy = throttleBy.toMillis();
    this.defaultRate = rateLimiter.getRate();
  }

  /**
   * @see RateLimiter#acquire()
   */
  public void acquire() {
    rateLimiter.acquire();
    LOGGER.debug("Acquired API ticket for {}", exchangeName);
    if (throttleExpired()) {
      synchronized (this) {
        if (throttleExpired()) {
          throttleExpiryTime = 0L;
          throttleLevel = 0;
          rateLimiter.setRate(defaultRate);
          LOGGER.info("Throttle on {} expired. Restored rate to {} calls/sec", exchangeName, rateLimiter.getRate());
        }
      }
    }
  }

  /**
   * Cuts the throughput rate significantly on a temporary basis. This throttle
   * will expire after a period of time.
   */
  public void throttle() {
    if (canThrottleFurther()) {
      synchronized (this) {
        if (canThrottleFurther()) {
          throttleExpiryTime = System.currentTimeMillis() + throttleBy;
          throttleLevel++;
          rateLimiter.setRate(rateLimiter.getRate() / DIVISOR);
          LOGGER.info("Throttled {} rate to {} calls/sec", exchangeName, rateLimiter.getRate());
        }
      }
    }
  }

  private boolean canThrottleFurther() {
    return throttleLevel < DEGREES_PERMITTED;
  }

  private boolean throttleExpired() {
    return throttleLevel != 0 && throttleExpiryTime < System.currentTimeMillis();
  }
}