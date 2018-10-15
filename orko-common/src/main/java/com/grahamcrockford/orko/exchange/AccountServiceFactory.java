package com.grahamcrockford.orko.exchange;

import org.knowm.xchange.service.account.AccountService;

import com.google.inject.ImplementedBy;

@ImplementedBy(AccountServiceFactoryImpl.class)
public interface AccountServiceFactory {

  public AccountService getForExchange(String exchange);

}
