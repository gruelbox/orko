/**
 * Orko - Copyright © 2018-2019 Graham Crockford
 *
 * <p>This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * <p>You should have received a copy of the GNU Affero General Public License along with this
 * program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.gruelbox.orko.auth.blacklist;

import static java.util.concurrent.TimeUnit.MINUTES;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.gruelbox.orko.auth.AuthConfiguration;
import com.gruelbox.orko.auth.RequestUtils;
import com.gruelbox.orko.util.SafelyDispose;
import io.dropwizard.lifecycle.Managed;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class Blacklisting implements Managed {

  private static final Logger LOGGER = LoggerFactory.getLogger(Blacklisting.class);

  private final Cache<String, AtomicInteger> blacklist;
  private final AuthConfiguration authConfiguration;
  private final Provider<RequestUtils> requestUtils;
  private final AtomicInteger attemptTickets = new AtomicInteger(0);

  private Disposable disposable;

  @Inject
  @VisibleForTesting
  public Blacklisting(Provider<RequestUtils> requestUtils, AuthConfiguration authConfiguration) {
    this.requestUtils = requestUtils;
    this.authConfiguration = authConfiguration;
    this.blacklist =
        CacheBuilder.newBuilder()
            .expireAfterAccess(authConfiguration.getBlacklistingExpirySeconds(), TimeUnit.SECONDS)
            .build();
  }

  @Override
  public void start() throws Exception {
    LOGGER.debug("Resetting available tickets");
    disposable = Observable.interval(1, MINUTES).subscribe(x -> attemptTickets.set(0));
  }

  @Override
  public void stop() throws Exception {
    SafelyDispose.of(disposable);
  }

  public void failure() {
    logGlobalFailure();
    logIpFailure();
  }

  private void logGlobalFailure() {
    int attempt = attemptTickets.incrementAndGet();
    if (attempt > 50) LOGGER.warn("Failed authentication attempt {} in last minute", attempt);
  }

  private void logIpFailure() {
    String ip = requestUtils.get().sourceIp();
    AtomicInteger count = blacklist.getIfPresent(ip);
    if (count == null) {
      synchronized (this) {
        count = blacklist.getIfPresent(ip);
        if (count == null) {
          count = new AtomicInteger(0);
          blacklist.put(ip, count);
        }
      }
    }
    if (count.incrementAndGet() == authConfiguration.getAttemptsBeforeBlacklisting())
      LOGGER.warn("Banned IP: {}", ip);
  }

  public void success() {
    blacklist.invalidate(requestUtils.get().sourceIp());
  }

  public boolean isBlacklisted() {
    if (isGloballyBlacklisted()) return true;
    return isIpBlacklisted();
  }

  private boolean isGloballyBlacklisted() {
    return attemptTickets.get() >= 100;
  }

  private boolean isIpBlacklisted() {
    String ip = requestUtils.get().sourceIp();
    AtomicInteger count = blacklist.getIfPresent(ip);
    boolean result =
        count != null && count.get() >= authConfiguration.getAttemptsBeforeBlacklisting();
    if (result) LOGGER.warn("Access attempt from banned IP: {}", ip);
    return result;
  }

  @VisibleForTesting
  public void cleanUp() {
    attemptTickets.set(0);
    blacklist.cleanUp();
  }
}
