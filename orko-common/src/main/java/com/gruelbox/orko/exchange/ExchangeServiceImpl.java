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

package com.gruelbox.orko.exchange;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.meta.CurrencyPairMetaData;
import org.knowm.xchange.dto.meta.ExchangeMetaData;
import org.knowm.xchange.dto.meta.RateLimit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import com.gruelbox.orko.OrkoConfiguration;
import com.gruelbox.orko.spi.TickerSpec;
import com.gruelbox.orko.util.CheckedExceptions;

import info.bitrich.xchangestream.core.StreamingExchangeFactory;

/**
 * API-friendly name mapping for exchanges.
 */
@Singleton
@VisibleForTesting
public class ExchangeServiceImpl implements ExchangeService {

  private static final long SENSIBLE_MINIMUM_POLL_DELAY = 2000;

  private static final Logger LOGGER = LoggerFactory.getLogger(ExchangeServiceImpl.class);

  private final OrkoConfiguration configuration;

  private final LoadingCache<String, Exchange> exchanges = CacheBuilder.newBuilder().build(new CacheLoader<String, Exchange>() {
    @Override
    public Exchange load(String name) throws Exception {
      final ExchangeConfiguration exchangeConfiguration = configuration.getExchanges() == null
        ? null
        : configuration.getExchanges().get(name);
      if (exchangeConfiguration == null || StringUtils.isEmpty(exchangeConfiguration.getApiKey()))
        return publicApi(name);
      return privateApi(name, exchangeConfiguration);
    }

    private Exchange publicApi(String name) {
      try {
        LOGGER.warn("No API connection details.  Connecting to public API: " + name);
        final ExchangeSpecification exSpec = createExchangeSpecification(name);
        return createExchange(exSpec);
      } catch (InstantiationException | IllegalAccessException e) {
        throw new IllegalArgumentException("Failed to connect to exchange [" + name + "]");
      }
    }

    private Exchange privateApi(String name, final ExchangeConfiguration exchangeConfiguration) {
      try {

        LOGGER.info("Connecting to private API: " + name);
        final ExchangeSpecification exSpec = createExchangeSpecification(name);
        if (name.equalsIgnoreCase(Exchanges.GDAX_SANDBOX) || name.equalsIgnoreCase(Exchanges.GDAX)) {
          exSpec.setExchangeSpecificParametersItem("passphrase", exchangeConfiguration.getPassphrase());
        }
        exSpec.setUserName(exchangeConfiguration.getUserName());
        exSpec.setApiKey(exchangeConfiguration.getApiKey());
        exSpec.setSecretKey(exchangeConfiguration.getSecretKey());
        return createExchange(exSpec);

      } catch (InstantiationException | IllegalAccessException e) {
        throw new IllegalArgumentException("Failed to connect to exchange [" + name + "]");
      }
    }

    private Exchange createExchange(final ExchangeSpecification exSpec) {
      if (exSpec.getExchangeClassName().contains("Streaming")) {
        return StreamingExchangeFactory.INSTANCE.createExchange(exSpec);
      } else {
        return ExchangeFactory.INSTANCE.createExchange(exSpec);
      }
    }

    private ExchangeSpecification createExchangeSpecification(String exchangeName) throws InstantiationException, IllegalAccessException {
      final ExchangeSpecification exSpec = Exchanges.friendlyNameToClass(exchangeName).newInstance().getDefaultExchangeSpecification();
      if (exchangeName.equalsIgnoreCase(Exchanges.GDAX_SANDBOX)) {
        LOGGER.info("Using sandbox GDAX");
        exSpec.setSslUri("https://api-public.sandbox.pro.coinbase.com");
        exSpec.setHost("api-public.sandbox.pro.coinbase.com");
      }
      return exSpec;
    }
  });

  private final LoadingCache<String, Long> safePollDelays = CacheBuilder.newBuilder().build(new CacheLoader<String, Long>() {
    @Override
    public Long load(String exchangeName) throws Exception {
      try {

        ExchangeMetaData metaData = get(exchangeName).getExchangeMetaData();

        Stream<RateLimit> rateLimits = Stream.empty();

        if (metaData.getPrivateRateLimits() != null)
          rateLimits = Arrays.asList(metaData.getPrivateRateLimits()).stream();
        if (metaData.getPublicRateLimits() != null)
          rateLimits = Stream.concat(rateLimits, Arrays.asList(metaData.getPublicRateLimits()).stream());

        // We floor the poll delay at a sensible minimum in case the above calculation goes
        // wrong (frequently when something's up with the exchange metadata).
        Optional<Long> limit = rateLimits
          .map(RateLimit::getPollDelayMillis)
          .max(Comparator.naturalOrder())
          .map(result -> {
            if (result < SENSIBLE_MINIMUM_POLL_DELAY) {
              LOGGER.warn("Exchange [[{}] reported suspicious pollDelayMillis ({}). Reset to {}",
                  exchangeName, result, SENSIBLE_MINIMUM_POLL_DELAY);
              return SENSIBLE_MINIMUM_POLL_DELAY;
            } else {
              return result;
            }
          });

        if (limit.isPresent()) {
          LOGGER.info("Safe poll delay for exchange [{}] is {}ms", exchangeName, limit.get());
          return limit.get();
        } else {
          LOGGER.info("Safe poll delay for exchange [{}] is unknown, defaulting to {}", exchangeName, SENSIBLE_MINIMUM_POLL_DELAY);
          return SENSIBLE_MINIMUM_POLL_DELAY;
        }

      } catch (Exception e) {
        LOGGER.warn("Failed to fetch exchange metadata for [" + exchangeName + "], defaulting to " + SENSIBLE_MINIMUM_POLL_DELAY + "ms", e);
        return SENSIBLE_MINIMUM_POLL_DELAY;
      }
    }
  });


  @Inject
  @VisibleForTesting
  public ExchangeServiceImpl(OrkoConfiguration configuration) {
    this.configuration = configuration;
  }


  /**
   * @see com.gruelbox.orko.exchange.ExchangeService#getExchanges()
   */
  @Override
  public Collection<String> getExchanges() {
    return ImmutableSet.<String>builder()
        .addAll(FluentIterable.from(Exchanges.EXCHANGE_TYPES.get())
                  .transform(Class::getSimpleName)
                  .transform(s -> s.replace("Exchange", ""))
                  .transform(String::toLowerCase)
                  .transform(s -> s.equals("coinbasepro") ? "gdax" : s)
        )
        .add(Exchanges.GDAX_SANDBOX)
        .build();
  }


  /**
   * @see com.gruelbox.orko.exchange.ExchangeService#get(java.lang.String)
   */
  @Override
  public Exchange get(String name) {
    return exchanges.getUnchecked(name);
  }


  /**
   * @see com.gruelbox.orko.exchange.ExchangeService#fetchTicker(com.gruelbox.orko.spi.TickerSpec)
   */
  @Override
  public Ticker fetchTicker(TickerSpec ex) {
    return CheckedExceptions.callUnchecked(() ->
      get(ex.exchange())
      .getMarketDataService()
      .getTicker(ex.currencyPair()));
  }


  /**
   * @see com.gruelbox.orko.exchange.ExchangeService#fetchCurrencyPairMetaData(com.gruelbox.orko.spi.TickerSpec)
   */
  @Override
  public CurrencyPairMetaData fetchCurrencyPairMetaData(TickerSpec ex) {
    return get(ex.exchange())
      .getExchangeMetaData()
      .getCurrencyPairs()
      .get(ex.currencyPair());
  }


  @Override
  public long safePollDelay(String exchangeName) {
    return safePollDelays.getUnchecked(exchangeName);
  }


  @Override
  public boolean exchangeSupportsPair(String exchange, CurrencyPair currencyPair) {
    return get(exchange)
        .getExchangeMetaData()
        .getCurrencyPairs()
        .keySet()
        .stream()
        .anyMatch(pair -> pair.equals(currencyPair));
  }
}
