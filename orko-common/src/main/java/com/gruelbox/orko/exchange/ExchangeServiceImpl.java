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

import static java.util.stream.Stream.concat;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.Nullable;
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
import com.google.common.base.MoreObjects;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Ordering;
import com.google.common.util.concurrent.RateLimiter;
import com.gruelbox.orko.OrkoConfiguration;
import com.gruelbox.orko.notification.NotificationService;
import com.gruelbox.orko.spi.TickerSpec;
import com.gruelbox.orko.util.CheckedExceptions;

import info.bitrich.xchangestream.core.StreamingExchangeFactory;
import info.bitrich.xchangestream.service.ConnectableService;

/**
 * API-friendly name mapping for exchanges.
 */
@Singleton
@VisibleForTesting
public class ExchangeServiceImpl implements ExchangeService {

  private static final RateLimit THROTTLED_RATE = new RateLimit(1, 10, TimeUnit.SECONDS);
  private static final RateLimit DEFAULT_RATE = new RateLimit(1, 3, TimeUnit.SECONDS);
  private static final RateLimit[] NO_LIMITS = new RateLimit[0];

  private static final Logger LOGGER = LoggerFactory.getLogger(ExchangeServiceImpl.class);

  private final OrkoConfiguration configuration;
  private final NotificationService notificationService;

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
        LOGGER.debug("No API connection details.  Connecting to public API: {}", name);
        final ExchangeSpecification exSpec = createExchangeSpecification(name);
        return createExchange(exSpec);
      } catch (InstantiationException | IllegalAccessException e) {
        throw new IllegalArgumentException("Failed to connect to exchange [" + name + "]");
      }
    }

    private Exchange privateApi(String name, final ExchangeConfiguration exchangeConfiguration) {
      try {

        LOGGER.debug("Connecting to private API: {}", name);
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
        LOGGER.debug("Using sandbox GDAX");
        exSpec.setSslUri("https://api-public.sandbox.pro.coinbase.com");
        exSpec.setHost("api-public.sandbox.pro.coinbase.com");
        RateLimiter rateLimiter = RateLimiter.create(0.25); // TODO make this exchange specific
        exSpec.setExchangeSpecificParametersItem(
          ConnectableService.BEFORE_CONNECTION_HANDLER,
          (Runnable) rateLimiter::acquire
        );
      }
      return exSpec;
    }
  });

  private final Cache<String, RateLimiter> throttledLimits = CacheBuilder.newBuilder().expireAfterWrite(Duration.ofSeconds(120)).build();

  private final LoadingCache<String, RateLimiter> rateLimiters = CacheBuilder.newBuilder().build(new CacheLoader<String, RateLimiter>() {
    @Override
    public RateLimiter load(String exchangeName) throws Exception {
      try {
        ExchangeMetaData metaData = get(exchangeName).getExchangeMetaData();
        return concat(asStream(metaData.getPrivateRateLimits()), asStream(metaData.getPublicRateLimits()))
            .max(Ordering.natural().onResultOf(RateLimit::getPollDelayMillis))
            .map(rateLimit -> {
              LOGGER.debug("Rate limit for [{}] is {}", exchangeName, DEFAULT_RATE);
              return asLimiter(rateLimit);
            })
            .orElseGet(() -> {
              LOGGER.debug("Rate limit for [{}] is unknown, defaulting to: {}", exchangeName, DEFAULT_RATE);
              return asLimiter(DEFAULT_RATE);
            });
      } catch (Exception e) {
        LOGGER.warn("Failed to fetch rate limit for [" + exchangeName + "], defaulting to " + DEFAULT_RATE, e);
        return asLimiter(DEFAULT_RATE);
      }
    }
  });

  @Inject
  @VisibleForTesting
  public ExchangeServiceImpl(OrkoConfiguration configuration, NotificationService notificationService) {
    this.configuration = configuration;
    this.notificationService = notificationService;
  }

  private static Stream<RateLimit> asStream(RateLimit[] limits) {
    return Arrays.stream(MoreObjects.firstNonNull(limits, NO_LIMITS));
  }

  private static RateLimiter asLimiter(RateLimit rateLimit) {
    double permitsPerSecond = ((double)rateLimit.calls / rateLimit.timeUnit.toSeconds(rateLimit.timeSpan)) - 0.01;
    LOGGER.debug("Permits per second = {}", permitsPerSecond);
    return RateLimiter.create(permitsPerSecond);
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
  public RateController rateLimiter(String exchangeName) {
    return () -> {
      @Nullable RateLimiter throttled = throttledLimits.getIfPresent(exchangeName);
      if (throttled == null) {
        rateLimiters.getUnchecked(exchangeName).acquire();
        LOGGER.debug("Acquired API ticket for {}", exchangeName);
      } else {
        throttled.acquire();
        LOGGER.debug("Acquired throttled API ticket for {}", exchangeName);
      }
    };
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


  @Override
  public void temporarilyThrottle(String exchange, String message) {
    if (throttledLimits.getIfPresent(exchange) == null) {
      notificationService.error("Throttling access to " + exchange + " due to server error (" + message + "). Check logs");
      throttledLimits.put(exchange, asLimiter(THROTTLED_RATE));
    }
  }
}
