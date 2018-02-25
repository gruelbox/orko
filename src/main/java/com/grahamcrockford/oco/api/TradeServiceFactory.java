package com.grahamcrockford.oco.api;

import org.knowm.xchange.service.trade.TradeService;

public interface TradeServiceFactory {

  public TradeService getForExchange(String exchange);

}
