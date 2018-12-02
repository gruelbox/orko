package com.gruelbox.orko.db;

import static org.junit.Assume.assumeFalse;

public class TestingUtils {

  private static final String SKIP_SLOW_TESTS = "skipSlowTests";

  public static void skipIfSlowTestsDisabled() {
    assumeFalse("true".equals(System.getProperty(SKIP_SLOW_TESTS)));
  }
}