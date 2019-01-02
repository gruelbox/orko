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
package com.gruelbox.orko.marketdata;

import java.math.BigDecimal;
import java.util.Date;

import javax.annotation.Nullable;

import org.knowm.xchange.dto.Order.OrderType;
import org.knowm.xchange.dto.marketdata.Trade;
import org.knowm.xchange.dto.trade.UserTrade;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.auto.value.AutoValue;
import com.gruelbox.orko.spi.TickerSpec;

/**
 * API version of {@link Trade}, which might change and is in any case not serializable.
 */
@AutoValue
@JsonDeserialize
public abstract class SerializableTrade {

  public static SerializableTrade create(String exchange, UserTrade userTrade) {
    return create(
      userTrade.getType(),
      userTrade.getOriginalAmount(),
      tickerFromTrade(exchange, userTrade),
      userTrade.getPrice(),
      userTrade.getTimestamp(),
      userTrade.getId(),
      userTrade.getOrderId(),
      userTrade.getFeeAmount(),
      userTrade.getFeeCurrency().getCurrencyCode());
  }

  public static SerializableTrade create(String exchange, org.knowm.xchange.dto.marketdata.Trade trade) {
    if (trade instanceof UserTrade)
      return create(exchange, (UserTrade) trade);
    return create(
      trade.getType(),
      trade.getOriginalAmount(),
      tickerFromTrade(exchange, trade),
      trade.getPrice(),
      trade.getTimestamp(),
      trade.getId(),
      null,
      null,
      null);
  }

  private static TickerSpec tickerFromTrade(String exchange, org.knowm.xchange.dto.marketdata.Trade trade) {
    return TickerSpec.builder()
      .exchange(exchange)
      .base(trade.getCurrencyPair().base.getCurrencyCode())
      .counter(trade.getCurrencyPair().counter.getCurrencyCode())
      .build();
  }

  @JsonCreator
  public static SerializableTrade create(@JsonProperty("t") OrderType type,
                             @JsonProperty("a") BigDecimal originalAmount,
                             @JsonProperty("c") TickerSpec spec,
                             @JsonProperty("p") BigDecimal price,
                             @JsonProperty("d") Date timestamp,
                             @JsonProperty("id") String id,
                             @JsonProperty("oid") String orderId,
                             @JsonProperty("fa") BigDecimal feeAmount,
                             @JsonProperty("fc") String feeCurrency) {
    return new AutoValue_SerializableTrade(type, originalAmount.toPlainString(), spec, price.toPlainString(), timestamp, id, orderId, feeAmount == null ? null : feeAmount.toPlainString(), feeCurrency);
  }

  /** Did this trade result from the execution of a bid or a ask? */
  @JsonProperty("t")
  public abstract OrderType type();

  /** Amount that was traded */
  @JsonProperty("a")
  public abstract String originalAmount();

  /** The currency pair */
  @JsonProperty("c")
  public abstract TickerSpec spec();

  /** The price */
  @JsonProperty("p")
  public abstract String price();

  /** The timestamp of the trade according to the exchange's server, null if not provided */
  @JsonProperty("d")
  public abstract Date timestamp();

  /** The trade id */
  @JsonProperty("id")
  @Nullable
  public abstract String id();

  /** The id of the order responsible for execution of this trade */
  @JsonProperty("oid")
  @Nullable
  public abstract String orderId();

  /** The fee that was charged by the exchange for this trade. */
  @JsonProperty("fa")
  @Nullable
  public abstract String feeAmount();

  /** The currency in which the fee was charged. */
  @JsonProperty("fc")
  @Nullable
  public abstract String feeCurrency();
}