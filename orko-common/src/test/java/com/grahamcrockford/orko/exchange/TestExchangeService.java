package com.grahamcrockford.orko.exchange;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.grahamcrockford.orko.OrkoConfiguration;

public class TestExchangeService {

  private ExchangeServiceImpl exchangeService;

  @Before
  public void setup() {
    exchangeService = new ExchangeServiceImpl(new OrkoConfiguration());
  }

  @Test
  public void testExchanges() {
    Assert.assertTrue(exchangeService.getExchanges().contains(Exchanges.GDAX));
    Assert.assertTrue(exchangeService.getExchanges().contains(Exchanges.GDAX_SANDBOX));
    Assert.assertTrue(exchangeService.getExchanges().contains(Exchanges.BINANCE));
    Assert.assertTrue(exchangeService.getExchanges().contains(Exchanges.BITFINEX));
    Assert.assertTrue(exchangeService.getExchanges().contains(Exchanges.KUCOIN));
    Assert.assertTrue(exchangeService.getExchanges().contains(Exchanges.BITTREX));
    Assert.assertTrue(exchangeService.getExchanges().contains(Exchanges.CRYPTOPIA));
  }
}