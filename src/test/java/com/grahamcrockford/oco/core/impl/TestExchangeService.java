package com.grahamcrockford.oco.core.impl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.knowm.xchange.binance.BinanceExchange;
import org.knowm.xchange.gdax.GDAXExchange;
import org.knowm.xchange.kucoin.KucoinExchange;

import com.grahamcrockford.oco.OcoConfiguration;
import com.grahamcrockford.oco.core.impl.ExchangeServiceImpl;

public class TestExchangeService {

  private ExchangeServiceImpl exchangeService;

  @Before
  public void setup() {
    exchangeService = new ExchangeServiceImpl(new OcoConfiguration());
  }

  @Test
  public void testExchanges() {
    Assert.assertTrue(exchangeService.getExchanges().contains("binance"));
    Assert.assertTrue(exchangeService.getExchanges().contains("kucoin"));
    Assert.assertTrue(exchangeService.getExchanges().contains("gdax"));
    Assert.assertTrue(exchangeService.getExchanges().contains("gdax-sandbox"));
    Assert.assertTrue(exchangeService.getExchanges().contains("cryptopia"));
    Assert.assertTrue(exchangeService.getExchanges().contains("bitfinex"));
    Assert.assertTrue(exchangeService.getExchanges().contains("bittrex"));
  }

  @Test
  public void testGdaxSandbox() {
    Assert.assertEquals(GDAXExchange.class, exchangeService.map("gdax-sandbox"));
  }

  @Test
  public void testGdax() {
    Assert.assertEquals(GDAXExchange.class, exchangeService.map("gdax"));
  }

  @Test
  public void testBinance() {
    Assert.assertEquals(BinanceExchange.class, exchangeService.map("binance"));
  }

  @Test
  public void testKucoin() {
    Assert.assertEquals(KucoinExchange.class, exchangeService.map("kucoin"));
  }
}