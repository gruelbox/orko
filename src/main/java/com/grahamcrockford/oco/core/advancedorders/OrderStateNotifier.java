package com.grahamcrockford.oco.core.advancedorders;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.auto.value.AutoValue;
import com.grahamcrockford.oco.api.AdvancedOrder;
import com.grahamcrockford.oco.api.AdvancedOrderInfo;
import com.grahamcrockford.oco.core.advancedorders.AutoValue_OrderStateNotifier;

@AutoValue
@JsonDeserialize(builder = OrderStateNotifier.Builder.class)
public abstract class OrderStateNotifier implements AdvancedOrder {

  public static final Builder builder() {
    return new AutoValue_OrderStateNotifier.Builder();
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "")
  public static abstract class Builder {
    @JsonCreator private static Builder create() { return OrderStateNotifier.builder(); }
    public abstract Builder id(long value);
    public abstract Builder basic(AdvancedOrderInfo exchangeInfo);
    public abstract Builder description(String description);
    public abstract Builder orderId(String orderId);
    public abstract OrderStateNotifier build();
  }

  @JsonIgnore
  public abstract Builder toBuilder();

  @Override
  @JsonProperty
  public abstract long id();

  @Override
  @JsonProperty
  public abstract AdvancedOrderInfo basic();

  @JsonProperty
  public abstract String description();

  @JsonProperty
  public abstract String orderId();


  @JsonIgnore
  @Override
  public final Class<OrderStateNotifierProcessor> processor() {
    return OrderStateNotifierProcessor.class;
  }
}