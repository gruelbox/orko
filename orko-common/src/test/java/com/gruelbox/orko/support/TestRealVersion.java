package com.gruelbox.orko.support;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestRealVersion {

  @Test
  public void testGetVersion() {
    assertEquals("Development version", ReadVersion.readVersionInfoInManifest());
  }

}
