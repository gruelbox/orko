package com.grahamcrockford.oco.core.advancedorders;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.grahamcrockford.oco.api.AdvancedOrder;
import com.grahamcrockford.oco.api.AdvancedOrderInfo;

@AutoValue
@JsonDeserialize(builder = PumpChecker.Builder.class)
public abstract class PumpChecker implements AdvancedOrder {

  public static final Builder builder() {
    return new AutoValue_PumpChecker.Builder()
        .priceHistory(ImmutableList.of());
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "")
  public static abstract class Builder {
    @JsonCreator private static Builder create() { return PumpChecker.builder(); }
    public abstract Builder id(String value);
    public abstract Builder basic(AdvancedOrderInfo exchangeInfo);
    public abstract Builder priceHistory(List<BigDecimal> history);
    public abstract PumpChecker build();
  }

  @JsonIgnore
  public abstract Builder toBuilder();

  @Override
  @JsonProperty
  public abstract String id();

  @JsonProperty
  public abstract List<BigDecimal> priceHistory();

  @Override
  @JsonProperty
  public abstract AdvancedOrderInfo basic();

  @JsonIgnore
  @Override
  public final Class<PumpCheckerProcessor> processor() {
    return PumpCheckerProcessor.class;
  }
}