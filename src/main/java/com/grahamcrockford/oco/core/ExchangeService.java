package com.grahamcrockford.oco.core;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;

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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.grahamcrockford.oco.OcoConfiguration;
import com.grahamcrockford.oco.api.TickerSpec;
import com.grahamcrockford.oco.util.CheckedExceptions;

/**
 * API-friendly name mapping for exchanges.
 */
@Singleton
public class ExchangeService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ExchangeService.class);

  private static final List<Class<? extends Exchange>> EXCHANGE_TYPES = new Reflections("org.knowm.xchange")
      .getSubTypesOf(Exchange.class)
      .stream()
      .filter(c -> !c.equals(BaseExchange.class))
      .collect(Collectors.toList());

  private final OcoConfiguration configuration;

  private final LoadingCache<String, Exchange> exchanges = CacheBuilder.newBuilder().build(new CacheLoader<String, Exchange>() {
    @Override
    public Exchange load(String name) throws Exception {
      if (configuration.getExchanges() == null)
        return publicApi(name);
      final ExchangeConfiguration exchangeConfiguration = configuration.getExchanges().get(name);
      if (exchangeConfiguration == null)
        return publicApi(name);
      if (exchangeConfiguration.getApiKey() == null || exchangeConfiguration.getApiKey().equals("default"))
        return publicApi(name);
      return privateApi(name, exchangeConfiguration);
    }

    private Exchange publicApi(String name) {
      LOGGER.warn("No API connection details.  Connecting to public API: " + name);
      return ExchangeFactory.INSTANCE.createExchange(map(name));
    }

    private Exchange privateApi(String name, final ExchangeConfiguration exchangeConfiguration) {
      try {
        LOGGER.info("Connecting to private API: " + name);
        final ExchangeSpecification exSpec = map(name).newInstance().getDefaultExchangeSpecification();

        if (name.toLowerCase().equals("gdax-sandbox")) {
          exSpec.setSslUri("https://api-public.sandbox.gdax.com");
          exSpec.setHost("api-public.sandbox.gdax.com");
        }

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

  });

  @Inject
  ExchangeService(OcoConfiguration configuration) {
    this.configuration = configuration;
  }

  public Collection<String> getExchanges() {
    return EXCHANGE_TYPES
        .stream()
        .map(Class::getSimpleName)
        .map(s -> s.replace("Exchange", ""))
        .map(String::toLowerCase)
        .collect(Collectors.toSet());
  }

  public Exchange get(String name) {
    return exchanges.getUnchecked(name);
  }

  public Ticker fetchTicker(TickerSpec ex) {
    return CheckedExceptions.callUnchecked(() ->
      get(ex.exchange())
      .getMarketDataService()
      .getTicker(ex.currencyPair()));
  }

  public CurrencyPairMetaData fetchCurrencyPairMetaData(TickerSpec ex) {
    return get(ex.exchange())
      .getExchangeMetaData()
      .getCurrencyPairs()
      .get(ex.currencyPair());
  }

  private Class<? extends Exchange> map(String friendlyName) {
    if (friendlyName.equals("gdax-sandbox"))
      return GDAXExchange.class;
    Optional<Class<? extends Exchange>> result = EXCHANGE_TYPES.stream()
        .filter(c -> c.getSimpleName().replace("Exchange", "").toLowerCase().equals(friendlyName))
        .findFirst();
    if (!result.isPresent())
      throw new IllegalArgumentException("Unknown exchange [" + friendlyName + "]");
    return result.get();
  }
}