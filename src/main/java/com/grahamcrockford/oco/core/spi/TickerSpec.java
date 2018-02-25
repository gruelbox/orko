package com.grahamcrockford.oco.core.spi;

import org.knowm.xchange.currency.CurrencyPair;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.auto.value.AutoValue;

/**
 * A generic trade request.
 */
@AutoValue
@JsonDeserialize(builder = TickerSpec.Builder.class)
public abstract class TickerSpec {

  public static Builder builder() {
    return new AutoValue_TickerSpec.Builder();
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "")
  public abstract static class Builder {
    @JsonCreator private static Builder create() { return TickerSpec.builder(); }
    public abstract Builder exchange(String value);
    public abstract Builder counter(String value);
    public abstract Builder base(String value);
    public abstract TickerSpec build();
  }

  @JsonIgnore
  public abstract Builder toBuilder();

  @JsonProperty
  public abstract String exchange();

  @JsonProperty
  public abstract String counter();

  @JsonProperty
  public abstract String base();

  @JsonIgnore
  public final String pairName() {
    return base() + "/" + counter();
  }

  @JsonIgnore
  public final CurrencyPair currencyPair() {
    return new CurrencyPair(base(), counter());
  }
}