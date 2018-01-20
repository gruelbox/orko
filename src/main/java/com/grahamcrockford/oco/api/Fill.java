package com.grahamcrockford.oco.api;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Fill {

  static Builder builder() {
    return new AutoValue_Fill.Builder();
  }

  @AutoValue.Builder
  abstract static class Builder {
    abstract Builder setBaseAmount(BigDecimal value);
    abstract Builder setPrice(BigDecimal value);
    abstract Builder setCounterAmount(BigDecimal value);
    abstract Builder setFeeCurrency(FeeCurrency value);
    abstract Fill build();
  }

  @JsonProperty
  public abstract BigDecimal baseAmount();

  @JsonProperty
  public abstract BigDecimal price();

  @JsonProperty
  public abstract BigDecimal counterAmount();

  @JsonProperty
  public abstract FeeCurrency feeCurrency();

  public enum FeeCurrency {
    BASE,
    COUNTER,
    OTHER
  }
}