package com.grahamcrockford.oco.core;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.knowm.xchange.BaseExchange;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.meta.CurrencyPairMetaData;
import org.knowm.xchange.gdax.GDAXExchange;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Suppliers;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import com.grahamcrockford.oco.OcoConfiguration;
import com.grahamcrockford.oco.api.exchange.ExchangeConfiguration;
import com.grahamcrockford.oco.api.exchange.ExchangeService;
import com.grahamcrockford.oco.api.util.CheckedExceptions;
import com.grahamcrockford.oco.spi.TickerSpec;

/**
 * API-friendly name mapping for exchanges.
 */
@Singleton
class ExchangeServiceImpl implements ExchangeService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ExchangeServiceImpl.class);

  private final OcoConfiguration configuration;
  private final Supplier<List<Class<? extends Exchange>>> exchangeTypes;


  private final LoadingCache<String, Exchange> exchanges = CacheBuilder.newBuilder().build(new CacheLoader<String, Exchange>() {
    @Override
    public Exchange load(String name) throws Exception {
      if (configuration.getExchanges() == null)
        return publicApi(name);
      final ExchangeConfiguration exchangeConfiguration = configuration.getExchanges().get(name);
      if (exchangeConfiguration == null)
        return publicApi(name);
      if (StringUtils.isEmpty(exchangeConfiguration.getApiKey()))
        return publicApi(name);
      return privateApi(name, exchangeConfiguration);
    }

    private Exchange publicApi(String name) {
      try {
        LOGGER.warn("No API connection details.  Connecting to public API: " + name);
        final ExchangeSpecification exSpec = createExchangeSpecification(name);
        return ExchangeFactory.INSTANCE.createExchange(exSpec);
      } catch (InstantiationException | IllegalAccessException e) {
        throw new IllegalArgumentException("Failed to connect to exchange [" + name + "]");
      }
    }

    private Exchange privateApi(String name, final ExchangeConfiguration exchangeConfiguration) {
      try {
        LOGGER.info("Connecting to private API: " + name);
        final ExchangeSpecification exSpec = createExchangeSpecification(name);

        if (name.toLowerCase().equals("gdax-sandbox") || name.toLowerCase().equals("gdax")) {
          exSpec.setExchangeSpecificParametersItem("passphrase", exchangeConfiguration.getPassphrase());
        }

        exSpec.setUserName(exchangeConfiguration.getUserName());
        exSpec.setApiKey(exchangeConfiguration.getApiKey());
        exSpec.setSecretKey(exchangeConfiguration.getSecretKey());

        return ExchangeFactory.INSTANCE.createExchange(exSpec);
      } catch (InstantiationException | IllegalAccessException e) {
        throw new IllegalArgumentException("Failed to connect to exchange [" + name + "]");
      }
    }

    private ExchangeSpecification createExchangeSpecification(String exchangeName) throws InstantiationException, IllegalAccessException {
      final ExchangeSpecification exSpec = map(exchangeName).newInstance().getDefaultExchangeSpecification();
      if (exchangeName.toLowerCase().equals("gdax-sandbox")) {
        LOGGER.info("Using sandbox GDAX");
        exSpec.setSslUri("https://api-public.sandbox.gdax.com");
        exSpec.setHost("api-public.sandbox.gdax.com");
      }
      return exSpec;
    }

  });

  @Inject
  ExchangeServiceImpl(OcoConfiguration configuration) {
    this.configuration = configuration;
    this.exchangeTypes = Suppliers.memoize(
        () -> new Reflections("org.knowm.xchange")
          .getSubTypesOf(Exchange.class)
          .stream()
          .filter(c -> !c.equals(BaseExchange.class))
          .collect(Collectors.toList()));
  }

  /**
   * @see com.grahamcrockford.oco.api.exchange.ExchangeService#getExchanges()
   */
  @Override
  public Collection<String> getExchanges() {
    return ImmutableSet.<String>builder()
        .addAll(FluentIterable.from(exchangeTypes.get())
                  .transform(Class::getSimpleName)
                  .transform(s -> s.replace("Exchange", ""))
                  .transform(String::toLowerCase))
        .add("gdax-sandbox")
        .build();
  }

  /**
   * @see com.grahamcrockford.oco.api.exchange.ExchangeService#get(java.lang.String)
   */
  @Override
  public Exchange get(String name) {
    return exchanges.getUnchecked(name);
  }

  /**
   * @see com.grahamcrockford.oco.api.exchange.ExchangeService#fetchTicker(com.grahamcrockford.oco.spi.TickerSpec)
   */
  @Override
  public Ticker fetchTicker(TickerSpec ex) {
    return CheckedExceptions.callUnchecked(() ->
      get(ex.exchange())
      .getMarketDataService()
      .getTicker(ex.currencyPair()));
  }

  /**
   * @see com.grahamcrockford.oco.api.exchange.ExchangeService#fetchCurrencyPairMetaData(com.grahamcrockford.oco.spi.TickerSpec)
   */
  @Override
  public CurrencyPairMetaData fetchCurrencyPairMetaData(TickerSpec ex) {
    return get(ex.exchange())
      .getExchangeMetaData()
      .getCurrencyPairs()
      .get(ex.currencyPair());
  }

  @VisibleForTesting
  Class<? extends Exchange> map(String friendlyName) {
    if (friendlyName.equals("gdax-sandbox"))
      return GDAXExchange.class;
    Optional<Class<? extends Exchange>> result = exchangeTypes.get()
        .stream()
        .filter(c -> c.getSimpleName().replace("Exchange", "").toLowerCase().equals(friendlyName))
        .findFirst();
    if (!result.isPresent())
      throw new IllegalArgumentException("Unknown exchange [" + friendlyName + "]");
    return result.get();
  }
}