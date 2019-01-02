package com.gruelbox.orko.exchange;

/*-
 * ===============================================================================L
 * Orko Common
 * ================================================================================
 * Copyright (C) 2018 - 2019 Graham Crockford
 * ================================================================================
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * ===============================================================================E
 */

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.knowm.xchange.BaseExchange;
import org.knowm.xchange.Exchange;
import org.reflections.Reflections;

import com.google.common.base.Suppliers;

import info.bitrich.xchangestream.coinbasepro.CoinbaseProStreamingExchange;
import info.bitrich.xchangestream.core.StreamingExchange;

public final class Exchanges {

  private Exchanges() {
    // Not constructable
  }

  public static final String BINANCE = "binance";
  public static final String GDAX = "gdax";
  public static final String GDAX_SANDBOX = "gdax-sandbox";
  public static final String KUCOIN = "kucoin";
  public static final String BITTREX = "bittrex";
  public static final String BITFINEX = "bitfinex";
  public static final String CRYPTOPIA = "cryptopia";
  public static final String BITMEX = "bitmex";

  static final Supplier<List<Class<? extends Exchange>>> EXCHANGE_TYPES = Suppliers.memoize(
      () -> new Reflections("org.knowm.xchange")
      .getSubTypesOf(Exchange.class)
      .stream()
      .filter(c -> !c.equals(BaseExchange.class))
      .collect(Collectors.toList()));

  static final Supplier<List<Class<? extends StreamingExchange>>> STREAMING_EXCHANGE_TYPES = Suppliers.memoize(
      () -> new Reflections("info.bitrich.xchangestream")
      .getSubTypesOf(StreamingExchange.class)
      .stream()
      .collect(Collectors.toList()));


  /**
   * Converts the friendly name for an exchange into the exchange class.
   *
   * @param friendlyName The friendly class name.
   * @return The exchange class
   */
  public static Class<? extends Exchange> friendlyNameToClass(String friendlyName) {

    if (friendlyName.equals(GDAX) || friendlyName.equals(GDAX_SANDBOX))
      return CoinbaseProStreamingExchange.class;

    Optional<Class<? extends StreamingExchange>> streamingResult = STREAMING_EXCHANGE_TYPES.get()
        .stream()
        .filter(c -> c.getSimpleName().replace("StreamingExchange", "").equalsIgnoreCase(friendlyName))
        .findFirst();
    if (streamingResult.isPresent())
      return streamingResult.get();

    Optional<Class<? extends Exchange>> result = EXCHANGE_TYPES.get()
        .stream()
        .filter(c -> c.getSimpleName().replace("Exchange", "").equalsIgnoreCase(friendlyName))
        .findFirst();
    if (!result.isPresent())
      throw new IllegalArgumentException("Unknown exchange [" + friendlyName + "]");
    return result.get();
  }
}
