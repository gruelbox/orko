package com.grahamcrockford.oco.core.api;

import org.knowm.xchange.service.trade.TradeService;

public interface TradeServiceFactory {

  public TradeService getForExchange(String exchange);

}
