package com.grahamcrockford.orko.db;

import org.mongojack.Id;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.grahamcrockford.orko.marketdata.MarketDataSubscription;
import com.grahamcrockford.orko.marketdata.MarketDataType;
import com.grahamcrockford.orko.spi.TickerSpec;

/**
 * Envelope used for storage of subscriptions in database
 */
@AutoValue
abstract class DbSubscription {

  @JsonCreator
  public static final DbSubscription create(@JsonProperty("id") @Id String id,
                                            @JsonProperty("spec") TickerSpec spec,
                                            @JsonProperty("type") MarketDataType type) {
    return new AutoValue_DbSubscription(id, spec, type);
  }

  public static final DbSubscription create(MarketDataSubscription subscription) {
    return new AutoValue_DbSubscription(
      subscription.key(),
      subscription.spec(),
      subscription.type()
    );
  }

  @JsonProperty
  @Id
  public abstract String id();

  @JsonProperty
  public abstract TickerSpec spec();

  @JsonProperty
  public abstract MarketDataType type();

  @JsonIgnore
  public final MarketDataSubscription toSubscription() {
    return MarketDataSubscription.create(spec(), type());
  }
}