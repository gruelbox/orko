package com.grahamcrockford.orko.job;

import java.math.BigDecimal;
import java.util.Map;

import javax.annotation.Nullable;

import org.mongojack.Id;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;
import com.grahamcrockford.orko.spi.Job;
import com.grahamcrockford.orko.spi.JobBuilder;
import com.grahamcrockford.orko.spi.JobControl;
import com.grahamcrockford.orko.spi.JobProcessor;
import com.grahamcrockford.orko.spi.TickerSpec;

@AutoValue
@JsonDeserialize(builder = LimitOrderJob.Builder.class)
public abstract class LimitOrderJob implements Job {

  public static final Builder builder() {
    return new AutoValue_LimitOrderJob.Builder();
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "")
  public static abstract class Builder implements JobBuilder<LimitOrderJob> {

    @JsonCreator private static Builder create() { return LimitOrderJob.builder(); }

    @Override
    @Id public abstract Builder id(String value);

    public abstract Builder tickTrigger(TickerSpec tickTrigger);
    public abstract Builder amount(BigDecimal amount);
    public abstract Builder limitPrice(BigDecimal value);
    public abstract Builder direction(Direction direction);

    final Builder bigDecimals(Map<String, String> values) {
      amount(new BigDecimal(values.get("amount")));
      limitPrice(new BigDecimal(values.get("limitPrice")));
      return this;
    }
    @Override
    public abstract LimitOrderJob build();
  }

  @Override
  @JsonIgnore
  public abstract Builder toBuilder();

  @Override
  @JsonProperty
  @Nullable
  @Id
  public abstract String id();

  @JsonProperty
  public abstract TickerSpec tickTrigger();

  @JsonProperty public abstract Direction direction();

  @JsonIgnore public abstract BigDecimal amount();
  @JsonIgnore public abstract BigDecimal limitPrice();

  @JsonProperty
  final Map<String, String> bigDecimals() {
    return ImmutableMap.<String, String>builder()
        .put("amount", amount().toPlainString())
        .put("limitPrice", limitPrice().toPlainString())
        .build();
  }

  @Override
  public String toString() {
    return String.format("%s order: %s %s at %s on %s", direction(), amount(), tickTrigger().base(), limitPrice(), tickTrigger());
  }

  @JsonIgnore
  @Override
  public final Class<Processor.Factory> processorFactory() {
    return Processor.Factory.class;
  }

  public enum Direction {
    BUY, SELL
  }

  public interface Processor extends JobProcessor<LimitOrderJob> {
    public interface Factory extends JobProcessor.Factory<LimitOrderJob> {
      @Override
      Processor create(LimitOrderJob job, JobControl jobControl);
    }
  }
}