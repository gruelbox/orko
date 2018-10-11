package com.grahamcrockford.oco.job;

import javax.annotation.Nullable;

import org.mongojack.Id;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.auto.value.AutoValue;
import com.grahamcrockford.oco.notification.Notification;
import com.grahamcrockford.oco.notification.NotificationLevel;
import com.grahamcrockford.oco.spi.Job;
import com.grahamcrockford.oco.spi.JobBuilder;
import com.grahamcrockford.oco.spi.JobControl;
import com.grahamcrockford.oco.spi.JobProcessor;

@AutoValue
@JsonDeserialize(builder = Alert.Builder.class)
public abstract class Alert implements Job {

  public static final Builder builder() {
    return new AutoValue_Alert.Builder()


        .notification(Notification.create("", NotificationLevel.INFO)); // TODO remove when production upgraded
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "")
  public static abstract class Builder implements JobBuilder<Alert> {
    @JsonCreator private static Builder create() { return Alert.builder(); }
    @Override
    @Id
    public abstract Builder id(String value);
    public abstract Builder notification(Notification notification);

 // TODO remove when production upgraded
    abstract Notification notification();

    // TODO remove when production upgraded
    public Builder message(String message) {
      notification(Notification.create(message, notification().level()));
      return this;
    }
    // TODO remove when production upgraded
    public Builder level(NotificationLevel level) {
      notification(Notification.create(notification().message(), level));
      return this;
    }

    @Override
    public abstract Alert build();
  }

  @Override
  @JsonIgnore
  public abstract Builder toBuilder();

  @Override
  @JsonProperty
  @Id
  @Nullable
  public abstract String id();

  @JsonProperty
  @Nullable // TODO remove when production upgraded
  public abstract Notification notification();

  @Override
  public String toString() {
    return String.format("send " + notification().level().toString().toLowerCase() + " '%s'", notification().message());
  }

  @JsonIgnore
  @Override
  public final Class<Processor.Factory> processorFactory() {
    return Processor.Factory.class;
  }

  public interface Processor extends JobProcessor<Alert> {
    public interface Factory extends JobProcessor.Factory<Alert> {
      @Override
      Processor create(Alert job, JobControl jobControl);
    }
  }
}