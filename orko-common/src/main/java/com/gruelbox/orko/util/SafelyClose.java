package com.gruelbox.orko.util;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SafelyClose {

  private static final Logger LOGGER = LoggerFactory.getLogger(SafelyClose.class);

  public static void the(AutoCloseable... closeables) {
    the(Arrays.asList(closeables));
  }

  public static void the(Iterable<AutoCloseable> closeables) {
    closeables.forEach(d -> {
      if (d == null)
        return;
      try {
        d.close();
      } catch (Exception e) {
        LOGGER.error("Error when closing resource", e);
      }
    });
  }
}