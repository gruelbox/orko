package com.grahamcrockford.oco.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.grahamcrockford.oco.marketdata.MarketDataSubscriptionManager;

import io.reactivex.disposables.Disposable;

public class SafelyDispose {

  private static final Logger LOGGER = LoggerFactory.getLogger(MarketDataSubscriptionManager.class);

  public static void of(Iterable<Disposable> disposables) {
    disposables.forEach(d -> {
      try {
        d.dispose();
      } catch (Exception e) {
        LOGGER.error("Error disposing of subscription", e);
      }
    });
  }
}