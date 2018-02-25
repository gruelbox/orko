package com.grahamcrockford.oco.core.jobs;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

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
import com.grahamcrockford.oco.spi.Job;
import com.grahamcrockford.oco.spi.JobBuilder;
import com.grahamcrockford.oco.spi.TickerSpec;

@AutoValue
@JsonDeserialize(builder = SoftTrailingStop.Builder.class)
public abstract class SoftTrailingStop implements Job {

  public static final Builder builder() {
    return new AutoValue_SoftTrailingStop.Builder()
        .limitPrice(BigDecimal.ZERO);
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "")
  public static abstract class Builder implements JobBuilder<SoftTrailingStop> {

    @JsonCreator private static Builder create() { return SoftTrailingStop.builder(); }

    @Override
    @Id @ObjectId public abstract Builder id(String value);
    public abstract Builder tickTrigger(TickerSpec tickTrigger);
    public abstract Builder amount(BigDecimal amount);
    public abstract Builder startPrice(BigDecimal value);
    abstract Builder lastSyncPrice(BigDecimal value);
    public abstract Builder stopPrice(BigDecimal value);
    public abstract Builder limitPrice(BigDecimal value);

    final Builder bigDecimals(Map<String, String> values) {
      amount(new BigDecimal(values.get("amount")));
      startPrice(new BigDecimal(values.get("startPrice")));
      lastSyncPrice(new BigDecimal(values.get("lastSyncPrice")));
      stopPrice(new BigDecimal(values.get("stopPrice")));
      limitPrice(new BigDecimal(values.get("limitPrice")));
      return this;
    }

    abstract BigDecimal startPrice();
    abstract Optional<BigDecimal> lastSyncPrice();
    abstract SoftTrailingStop autoBuild();

    @Override
    public SoftTrailingStop build() {
      if (!lastSyncPrice().isPresent()) {
        lastSyncPrice(startPrice());
      }
      return autoBuild();
    }
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
  @JsonIgnore public abstract BigDecimal startPrice();
  @JsonIgnore public abstract BigDecimal lastSyncPrice();
  @JsonIgnore public abstract BigDecimal stopPrice();
  @JsonIgnore public abstract BigDecimal limitPrice();

  @JsonProperty
  final Map<String, String> bigDecimals() {
    return ImmutableMap.<String, String>builder()
        .put("amount", amount().toPlainString())
        .put("startPrice", startPrice().toPlainString())
        .put("lastSyncPrice", lastSyncPrice().toPlainString())
        .put("stopPrice", stopPrice().toPlainString())
        .put("limitPrice", limitPrice().toPlainString())
        .build();
  }

  @JsonIgnore
  @Override
  public final Class<SoftTrailingStopProcessor> processor() {
    return SoftTrailingStopProcessor.class;
  }
}