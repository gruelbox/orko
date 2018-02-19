package com.grahamcrockford.oco.core.jobs;

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
import com.grahamcrockford.oco.api.Job;
import com.grahamcrockford.oco.api.JobBuilder;
import com.grahamcrockford.oco.api.TickerSpec;

@AutoValue
@JsonDeserialize(builder = PumpChecker.Builder.class)
public abstract class PumpChecker implements Job {

  public static final Builder builder() {
    return new AutoValue_PumpChecker.Builder()
        .priceHistory(ImmutableList.of());
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "")
  public static abstract class Builder implements JobBuilder {
    @JsonCreator private static Builder create() { return PumpChecker.builder(); }
    @Override
    @Id @ObjectId
    public abstract Builder id(String value);
    public abstract Builder tickTrigger(TickerSpec tickTrigger);
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
  public abstract TickerSpec tickTrigger();

  @JsonIgnore
  @Override
  public final Class<PumpCheckerProcessor> processor() {
    return PumpCheckerProcessor.class;
  }
}