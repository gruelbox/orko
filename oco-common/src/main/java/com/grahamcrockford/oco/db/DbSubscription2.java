package com.grahamcrockford.oco.db;

import javax.annotation.Nullable;

import org.mongojack.Id;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.grahamcrockford.oco.spi.TickerSpec;

/**
 * Envelope used for storage of subscriptions in database
 */
@AutoValue
abstract class DbSubscription2 {

  @JsonCreator
  public static final DbSubscription2 create(@JsonProperty("id") @Id String id,
                                             @JsonProperty("spec") TickerSpec spec,
                                             @JsonProperty("referencePrice") String referencePrice) {
    return new AutoValue_DbSubscription2(id, spec, referencePrice);
  }

  @JsonCreator
  public static final DbSubscription2 create(@JsonProperty("spec") TickerSpec spec) {
    return new AutoValue_DbSubscription2(spec.key(), spec, null);
  }

  @JsonProperty
  @Id
  public abstract String id();

  @JsonProperty
  public abstract TickerSpec spec();

  @JsonProperty
  @Nullable
  public abstract String referencePrice();
}