package com.gruelbox.orko.exchange;

import org.knowm.xchange.service.trade.TradeService;

import com.google.inject.ImplementedBy;

@ImplementedBy(TradeServiceFactoryImpl.class)
public interface TradeServiceFactory {

  public TradeService getForExchange(String exchange);

}
