package com.gruelbox.orko.exchange;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.knowm.xchange.BaseExchange;
import org.knowm.xchange.Exchange;
import org.reflections.Reflections;

import com.google.common.base.Suppliers;

import info.bitrich.xchangestream.core.StreamingExchange;
import info.bitrich.xchangestream.gdax.GDAXStreamingExchange;

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

    if (friendlyName.equals(GDAX_SANDBOX))
      return GDAXStreamingExchange.class;

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