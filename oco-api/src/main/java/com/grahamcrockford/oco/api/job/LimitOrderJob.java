package com.grahamcrockford.oco.api.job;

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
import com.grahamcrockford.oco.spi.Job;
import com.grahamcrockford.oco.spi.JobBuilder;
import com.grahamcrockford.oco.spi.JobControl;
import com.grahamcrockford.oco.spi.JobProcessor;
import com.grahamcrockford.oco.spi.TickerSpec;

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
    @Id @ObjectId public abstract Builder id(String value);

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
  @Id @ObjectId
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