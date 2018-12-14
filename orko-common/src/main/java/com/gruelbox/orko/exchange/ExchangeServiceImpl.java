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

  private final LoadingCache<String, Optional<Long>> safePollDelays = CacheBuilder.newBuilder().build(new CacheLoader<String, Optional<Long>>() {
    @Override
    public Optional<Long> load(String exchangeName) throws Exception {
      try {

        ExchangeMetaData metaData = get(exchangeName).getExchangeMetaData();

        Stream<RateLimit> rateLimits = Stream.empty();

        if (metaData.getPrivateRateLimits() != null)
          rateLimits = Arrays.asList(metaData.getPrivateRateLimits()).stream();
        if (metaData.getPublicRateLimits() != null)
          rateLimits = Stream.concat(rateLimits, Arrays.asList(metaData.getPublicRateLimits()).stream());

        // We floor the poll delay at a sensible minimum in case the above calculation goes
        // wrong (frequently when something's up with the exchange metadata).
        return rateLimits
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

      } catch (Exception e) {
        LOGGER.warn("Failed to fetch exchange metadata for " + exchangeName, e);
        return Optional.empty();
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
  public Optional<Long> safePollDelay(String exchangeName) {
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