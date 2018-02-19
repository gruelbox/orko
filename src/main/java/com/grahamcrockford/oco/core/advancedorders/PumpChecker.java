package com.grahamcrockford.oco.core.advancedorders;

import java.math.BigDecimal;
import java.util.List;

import javax.annotation.Nullable;

import org.mongojack.Id;
import org.mongojack.ObjectId;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.auto.value.AutoValue;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.grahamcrockford.oco.api.AdvancedOrder;
import com.grahamcrockford.oco.api.AdvancedOrderBuilder;
import com.grahamcrockford.oco.api.TickTrigger;

@AutoValue
@JsonDeserialize(builder = PumpChecker.Builder.class)
public abstract class PumpChecker implements AdvancedOrder {

  public static final Builder builder() {
    return new AutoValue_PumpChecker.Builder()
        .priceHistory(ImmutableList.of());
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "")
  public static abstract class Builder implements AdvancedOrderBuilder {
    @JsonCreator private static Builder create() { return PumpChecker.builder(); }
    @Override
    @Id @ObjectId
    public abstract Builder id(String value);
    public abstract Builder tickTrigger(TickTrigger tickTrigger);
    public abstract Builder priceHistory(List<BigDecimal> history);

    final Builder priceHistoryStr(List<String> history) {
      priceHistory(FluentIterable.from(history).transform(BigDecimal::new).toList());
      return this;
    }

    @Override
    public abstract PumpChecker build();
  }

  @Override
  @JsonIgnore
  public abstract Builder toBuilder();

  @Override
  @JsonProperty
  @Id @ObjectId
  @Nullable
  public abstract String id();

  @JsonIgnore
  public abstract List<BigDecimal> priceHistory();

  @JsonProperty
  final List<String> priceHistoryStr() {
    return FluentIterable.from(priceHistory()).transform(BigDecimal::toPlainString).toList();
  }

  @JsonProperty
  public abstract TickTrigger tickTrigger();

  @JsonIgnore
  @Override
  public final Class<PumpCheckerProcessor> processor() {
    return PumpCheckerProcessor.class;
  }
}