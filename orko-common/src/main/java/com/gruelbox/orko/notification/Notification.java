package com.gruelbox.orko.notification;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.auto.value.AutoValue;

/**
 * Event fired when the user should be alerted of something.
 */
@AutoValue
@JsonDeserialize
public abstract class Notification {

  @JsonCreator
  public static Notification create(@JsonProperty("message") String message, @JsonProperty("level") NotificationLevel level) {
    return new AutoValue_Notification(message, level);
  }

  @JsonProperty
  public abstract String message();

  @JsonProperty
  public abstract NotificationLevel level();
}