package com.grahamcrockford.orko.marketdata;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.auto.value.AutoValue;

@AutoValue
@JsonDeserialize
public abstract class BalanceEvent {

  @JsonCreator
  public static BalanceEvent create(@JsonProperty("exchange") String exchange,
                                    @JsonProperty("currency") String currency,
                                    @JsonProperty("balance") Balance balance) {
    return new AutoValue_BalanceEvent(exchange, currency, balance);
  }

  @JsonProperty
  public abstract String exchange();

  @JsonProperty
  public abstract String currency();

  @JsonProperty
  public abstract Balance balance();
}