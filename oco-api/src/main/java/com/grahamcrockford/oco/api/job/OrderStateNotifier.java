package com.grahamcrockford.oco.api.job;

import javax.annotation.Nullable;

import org.mongojack.Id;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.auto.value.AutoValue;
import com.grahamcrockford.oco.spi.Job;
import com.grahamcrockford.oco.spi.JobBuilder;
import com.grahamcrockford.oco.spi.JobControl;
import com.grahamcrockford.oco.spi.JobProcessor;
import com.grahamcrockford.oco.spi.TickerSpec;

@AutoValue
@JsonDeserialize(builder = OrderStateNotifier.Builder.class)
public abstract class OrderStateNotifier implements Job {

  public static final Builder builder() {
    return new AutoValue_OrderStateNotifier.Builder();
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "")
  public static abstract class Builder implements JobBuilder<OrderStateNotifier> {
    @JsonCreator private static Builder create() { return OrderStateNotifier.builder(); }
    @Override
    @Id
    public abstract Builder id(String value);
    public abstract Builder tickTrigger(TickerSpec tickTrigger);
    public abstract Builder description(String description);
    public abstract Builder orderId(String orderId);
    @Override
    public abstract OrderStateNotifier build();
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

  @JsonProperty
  public abstract String description();

  @JsonProperty
  public abstract String orderId();


  @JsonIgnore
  @Override
  public final Class<Processor.Factory> processorFactory() {
    return Processor.Factory.class;
  }

  public interface Processor extends JobProcessor<OrderStateNotifier> {
    public interface Factory extends JobProcessor.Factory<OrderStateNotifier> {
      @Override
      Processor create(OrderStateNotifier job, JobControl jobControl);
    }
  }
}