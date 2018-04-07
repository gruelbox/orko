package com.grahamcrockford.oco.api.job;

import java.math.BigDecimal;
import java.util.List;

import javax.annotation.Nullable;

import org.mongojack.Id;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.auto.value.AutoValue;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.grahamcrockford.oco.spi.Job;
import com.grahamcrockford.oco.spi.JobBuilder;
import com.grahamcrockford.oco.spi.JobControl;
import com.grahamcrockford.oco.spi.JobProcessor;
import com.grahamcrockford.oco.spi.TickerSpec;

@AutoValue
@JsonDeserialize(builder = PumpChecker.Builder.class)
public abstract class PumpChecker implements Job {

  public static final Builder builder() {
    return new AutoValue_PumpChecker.Builder()
        .priceHistory(ImmutableList.of());
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "")
  public static abstract class Builder implements JobBuilder<PumpChecker> {
    @JsonCreator private static Builder create() { return PumpChecker.builder(); }
    @Override
    @Id
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
  @Id
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
  public final Class<Processor.Factory> processorFactory() {
    return Processor.Factory.class;
  }

  public interface Processor extends JobProcessor<PumpChecker> {
    public interface Factory extends JobProcessor.Factory<PumpChecker> {
      @Override
      Processor create(PumpChecker job, JobControl jobControl);
    }
  }
}