package com.grahamcrockford.oco.core.jobs;

import java.math.BigDecimal;
import java.util.Map;
import javax.annotation.Nullable;

import org.mongojack.Id;
import org.mongojack.ObjectId;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;
import com.grahamcrockford.oco.core.spi.Job;
import com.grahamcrockford.oco.core.spi.JobBuilder;
import com.grahamcrockford.oco.core.spi.TickerSpec;

@AutoValue
@JsonDeserialize(builder = LimitSell.Builder.class)
public abstract class LimitSell implements Job {

  public static final Builder builder() {
    return new AutoValue_LimitSell.Builder();
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "")
  public static abstract class Builder implements JobBuilder<LimitSell> {

    @JsonCreator private static Builder create() { return LimitSell.builder(); }

    @Override
    @Id @ObjectId public abstract Builder id(String value);

    public abstract Builder tickTrigger(TickerSpec tickTrigger);
    public abstract Builder amount(BigDecimal amount);
    public abstract Builder limitPrice(BigDecimal value);

    final Builder bigDecimals(Map<String, String> values) {
      amount(new BigDecimal(values.get("amount")));
      limitPrice(new BigDecimal(values.get("limitPrice")));
      return this;
    }
    @Override
    public abstract LimitSell build();
  }

  @Override
  @JsonIgnore
  public abstract Builder toBuilder();

  @Override
  @JsonProperty
  @Nullable
  @Id @ObjectId
  public abstract String id();

  @JsonProperty
  public abstract TickerSpec tickTrigger();

  @JsonIgnore public abstract BigDecimal amount();
  @JsonIgnore public abstract BigDecimal limitPrice();

  @JsonProperty
  final Map<String, String> bigDecimals() {
    return ImmutableMap.<String, String>builder()
        .put("amount", amount().toPlainString())
        .put("limitPrice", limitPrice().toPlainString())
        .build();
  }

  @JsonIgnore
  @Override
  public final Class<LimitSellProcessor.Factory> processorFactory() {
    return LimitSellProcessor.Factory.class;
  }
}