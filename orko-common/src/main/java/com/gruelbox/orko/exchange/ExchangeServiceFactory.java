package com.gruelbox.orko.exchange;

interface ExchangeServiceFactory<T> {

  public T getForExchange(String exchange);

}
