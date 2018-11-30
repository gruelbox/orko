package com.gruelbox.orko.util;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reactivex.disposables.Disposable;

public class SafelyDispose {

  private static final Logger LOGGER = LoggerFactory.getLogger(SafelyDispose.class);

  public static void of(Disposable... disposables) {
    of(Arrays.asList(disposables));
  }

  public static void of(Iterable<Disposable> disposables) {
    disposables.forEach(d -> {
      if (d == null)
        return;
      try {
        d.dispose();
      } catch (Exception e) {
        LOGGER.error("Error disposing of subscription", e);
      }
    });
  }
}