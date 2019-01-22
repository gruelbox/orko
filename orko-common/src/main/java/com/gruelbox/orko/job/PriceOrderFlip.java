/**
 * Orko
 * Copyright © 2018-2019 Graham Crockford
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.gruelbox.orko.job;

import java.math.BigDecimal;

import javax.annotation.Nullable;

import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.StopOrder;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.auto.value.AutoValue;
import com.gruelbox.orko.jobrun.spi.Job;
import com.gruelbox.orko.jobrun.spi.JobBuilder;
import com.gruelbox.orko.jobrun.spi.JobControl;
import com.gruelbox.orko.jobrun.spi.JobProcessor;
import com.gruelbox.orko.spi.TickerSpec;

@AutoValue
@JsonDeserialize(builder = PriceOrderFlip.Builder.class)
public abstract class PriceOrderFlip implements Job {

  public static final Builder builder() {
    return new AutoValue_PriceOrderFlip.Builder().state(State.INACTIVE);
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "")
  public abstract static class Builder implements JobBuilder<PriceOrderFlip> {
    @JsonCreator private static Builder create() { return PriceOrderFlip.builder(); }
    @Override
    public abstract Builder id(String value);
    public abstract Builder tickTrigger(TickerSpec tickTrigger);
    public abstract Builder flipPrice(BigDecimal bigDecimal);
    public abstract Builder highOrder(Order order);
    public abstract Builder lowOrder(Order order);
    public abstract Builder state(State state);
    public abstract Builder activeOrderId(String orderId);

    @Override
    public abstract PriceOrderFlip build();
  }

  @Override
  @JsonIgnore
  public abstract Builder toBuilder();

  @Override
  @JsonProperty
  @Nullable
  public abstract String id();

  @JsonProperty
  public abstract TickerSpec tickTrigger();

  @JsonProperty
  public abstract BigDecimal flipPrice();

  @JsonProperty
  public abstract Order highOrder();

  @JsonProperty
  public abstract Order lowOrder();

  @JsonProperty
  public abstract State state();

  @JsonProperty
  @Nullable
  public abstract String activeOrderId();

  @Override
  public String toString() {
    return String.format("while price below %s on %s, maintain %s at %s. When above, maintain %s order at %s",
        flipPrice().toPlainString(),
        tickTrigger(),
        lowOrder().getClass().getSimpleName(),
        lowPrice(),
        highOrder().getClass().getSimpleName(),
        highPrice());
  }

  @JsonIgnore
  public BigDecimal lowPrice() {
    return (lowOrder() instanceof StopOrder)
      ? ((StopOrder)lowOrder()).getStopPrice()
      : ((LimitOrder)lowOrder()).getLimitPrice();
  }

  @JsonIgnore
  public BigDecimal highPrice() {
    return (highOrder() instanceof StopOrder)
      ? ((StopOrder)highOrder()).getStopPrice()
      : ((LimitOrder)highOrder()).getLimitPrice();
  }

  @JsonIgnore
  @Override
  public final Class<Processor.ProcessorFactory> processorFactory() {
    return Processor.ProcessorFactory.class;
  }

  public enum State {
    INACTIVE,
    HIGH_ACTIVE,
    LOW_ACTIVE
  }

  public interface Processor extends JobProcessor<PriceOrderFlip> {
    public interface ProcessorFactory extends JobProcessor.Factory<PriceOrderFlip> {
      @Override
      Processor create(PriceOrderFlip job, JobControl jobControl);
    }
  }
}