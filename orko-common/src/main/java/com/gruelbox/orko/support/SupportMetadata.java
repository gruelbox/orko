package com.gruelbox.orko.support;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class SupportMetadata {

  static SupportMetadata create(String version) {
    return new AutoValue_SupportMetadata(version);
  }

  @JsonProperty
  public abstract String version();

}