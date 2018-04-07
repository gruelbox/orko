package com.grahamcrockford.oco.api.exchange;

import org.knowm.xchange.service.account.AccountService;
import org.knowm.xchange.service.trade.TradeService;

import com.google.inject.ImplementedBy;

@ImplementedBy(AccountServiceFactoryImpl.class)
public interface AccountServiceFactory {

  public AccountService getForExchange(String exchange);

}
