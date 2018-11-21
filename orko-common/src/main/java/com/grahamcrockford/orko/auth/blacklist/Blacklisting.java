package com.grahamcrockford.orko.auth.blacklist;

import static io.reactivex.schedulers.Schedulers.single;
import static java.util.concurrent.TimeUnit.MINUTES;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.grahamcrockford.orko.auth.AuthConfiguration;
import com.grahamcrockford.orko.auth.RequestUtils;
import com.grahamcrockford.orko.util.SafelyDispose;

import io.dropwizard.lifecycle.Managed;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

@Singleton
public class Blacklisting implements Managed {
  
  private static final Logger LOGGER = LoggerFactory.getLogger(Blacklisting.class);
  
  private final Cache<String, AtomicInteger> blacklist;
  private AuthConfiguration authConfiguration;
  private Provider<RequestUtils> requestUtils;
  private AtomicInteger attemptTickets = new AtomicInteger(0);

  private Disposable disposable;

  @Inject
  @VisibleForTesting
  public Blacklisting(Provider<RequestUtils> requestUtils, AuthConfiguration authConfiguration) {
    this.requestUtils = requestUtils;
    this.authConfiguration = authConfiguration;
    this.blacklist = CacheBuilder.newBuilder().expireAfterAccess(authConfiguration.getBlacklistingExpirySeconds(), TimeUnit.SECONDS).build();
  }
  
  @Override
  public void start() throws Exception {
    LOGGER.debug("Resetting available tickets");
    disposable = Observable.interval(1, MINUTES).observeOn(single()).subscribe(x -> attemptTickets.set(0));
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
    if (attempt > 50)
      LOGGER.warn("Failed authentication attempt {} in last minute", attempt);
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
      LOGGER.warn("Banned IP: " + ip);
  }
  
  public void success() {
    blacklist.invalidate(requestUtils.get().sourceIp());
  }

  public boolean isBlacklisted() {
    if (isGloballyBlacklisted())
      return true;
    return isIpBlacklisted();
  }
  
  private boolean isGloballyBlacklisted() {
    return attemptTickets.get() >= 100;
  }

  private boolean isIpBlacklisted() {
    String ip = requestUtils.get().sourceIp();
    AtomicInteger count = blacklist.getIfPresent(ip);
    boolean result = count != null && count.get() >= authConfiguration.getAttemptsBeforeBlacklisting();
    if (result)
      LOGGER.warn("Access attempt from banned IP: " + ip);
    return result;
  }
  
  @VisibleForTesting
  public void cleanUp() {
    attemptTickets.set(0);
    blacklist.cleanUp();
  }
}