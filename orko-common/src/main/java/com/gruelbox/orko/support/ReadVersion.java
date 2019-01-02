package com.gruelbox.orko.support;

public final class ReadVersion {

  public static String readVersionInfoInManifest() {
    return ReadVersion.class.getPackage().getSpecificationVersion();
  }
}