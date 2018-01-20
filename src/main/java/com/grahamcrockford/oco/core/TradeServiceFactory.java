package com.grahamcrockford.oco.core;

import org.knowm.xchange.service.trade.TradeService;

import com.google.inject.ImplementedBy;

@ImplementedBy(LiveTradeServiceFactory.class)
public interface TradeServiceFactory {

  public TradeService getForExchange(String exchange);

}
