package com.grahamcrockford.oco.core.api;

import java.util.Collection;

import org.knowm.xchange.Exchange;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.meta.CurrencyPairMetaData;

import com.grahamcrockford.oco.core.spi.TickerSpec;

public interface ExchangeService {

  Collection<String> getExchanges();

  Exchange get(String name);

  Ticker fetchTicker(TickerSpec ex);

  CurrencyPairMetaData fetchCurrencyPairMetaData(TickerSpec ex);

}