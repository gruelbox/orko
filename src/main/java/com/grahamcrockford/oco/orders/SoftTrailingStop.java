package com.grahamcrockford.oco.orders;

import java.math.BigDecimal;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.auto.value.AutoValue;
import com.grahamcrockford.oco.api.AdvancedOrder;
import com.grahamcrockford.oco.api.AdvancedOrderInfo;

@AutoValue
@JsonDeserialize(builder = SoftTrailingStop.Builder.class)
public abstract class SoftTrailingStop implements AdvancedOrder {

  public static final Builder builder() {
    return new AutoValue_SoftTrailingStop.Builder()
        .limitPercentage(BigDecimal.ZERO);
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "")
  public static abstract class Builder {

    @JsonCreator private static Builder create() { return SoftTrailingStop.builder(); }

    public abstract Builder id(long value);
    public abstract Builder basic(AdvancedOrderInfo exchangeInfo);
    public abstract Builder amount(BigDecimal amount);
    public abstract Builder startPrice(BigDecimal value);
    abstract Builder lastSyncPrice(BigDecimal value);
    public abstract Builder stopPercentage(BigDecimal value);
    public abstract Builder limitPercentage(BigDecimal value);

    abstract BigDecimal startPrice();
    abstract Optional<BigDecimal> lastSyncPrice();
    abstract SoftTrailingStop autoBuild();

    public SoftTrailingStop build() {
      if (!lastSyncPrice().isPresent()) {
        lastSyncPrice(startPrice());
      }
      return autoBuild();
    }
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
  public abstract BigDecimal amount();

  @JsonProperty
  public abstract BigDecimal startPrice();

  @JsonProperty
  public abstract BigDecimal lastSyncPrice();

  @JsonProperty
  public abstract BigDecimal stopPercentage();

  @JsonProperty
  public abstract BigDecimal limitPercentage();

  @JsonIgnore
  @Override
  public final Class<SoftTrailingStopProcessor> processor() {
    return SoftTrailingStopProcessor.class;
  }
}