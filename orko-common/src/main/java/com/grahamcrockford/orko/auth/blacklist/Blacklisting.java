package com.grahamcrockford.orko.auth.blacklist;

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

@Singleton
public class Blacklisting {
  
  private static final Logger LOGGER = LoggerFactory.getLogger(Blacklisting.class);
  
  private final Cache<String, AtomicInteger> blacklist;
  private AuthConfiguration authConfiguration;
  private Provider<RequestUtils> requestUtils;

  @Inject
  @VisibleForTesting
  public Blacklisting(Provider<RequestUtils> requestUtils, AuthConfiguration authConfiguration) {
    this.requestUtils = requestUtils;
    this.authConfiguration = authConfiguration;
    this.blacklist = CacheBuilder.newBuilder().expireAfterAccess(authConfiguration.getBlacklistingExpirySeconds(), TimeUnit.SECONDS).build();
  }
  
  public void failure() {
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
  
  @VisibleForTesting
  public void cleanUp() {
    blacklist.cleanUp();
  }
  
  public boolean isBlacklisted() {
    String ip = requestUtils.get().sourceIp();
    AtomicInteger count = blacklist.getIfPresent(ip);
    boolean result = count != null && count.get() >= authConfiguration.getAttemptsBeforeBlacklisting();
    if (result)
      LOGGER.warn("Access attempt from banned IP: " + ip);
    return result;
  }
}