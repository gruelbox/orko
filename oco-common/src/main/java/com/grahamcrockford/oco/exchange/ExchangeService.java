package com.grahamcrockford.oco.exchange;

import java.util.Collection;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.meta.CurrencyPairMetaData;

import com.google.inject.ImplementedBy;
import com.grahamcrockford.oco.spi.TickerSpec;

@ImplementedBy(ExchangeServiceImpl.class)
public interface ExchangeService {

  Collection<String> getExchanges();

  Exchange get(String name);

  Ticker fetchTicker(TickerSpec ex);

  CurrencyPairMetaData fetchCurrencyPairMetaData(TickerSpec ex);

}