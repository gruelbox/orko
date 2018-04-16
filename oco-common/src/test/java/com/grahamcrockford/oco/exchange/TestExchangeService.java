package com.grahamcrockford.oco.exchange;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.knowm.xchange.bitmex.BitmexExchange;
import org.knowm.xchange.bittrex.BittrexExchange;
import org.knowm.xchange.kucoin.KucoinExchange;

import com.grahamcrockford.oco.OcoConfiguration;
import com.grahamcrockford.oco.exchange.ExchangeServiceImpl;

import info.bitrich.xchangestream.bitfinex.BitfinexStreamingExchange;
import info.bitrich.xchangestream.gdax.GDAXStreamingExchange;

public class TestExchangeService {

  private ExchangeServiceImpl exchangeService;

  @Before
  public void setup() {
    exchangeService = new ExchangeServiceImpl(new OcoConfiguration());
  }

  @Test
  public void testExchanges() {
    Assert.assertTrue(exchangeService.getExchanges().contains("gdax"));
    Assert.assertTrue(exchangeService.getExchanges().contains("gdax-sandbox"));
    Assert.assertTrue(exchangeService.getExchanges().contains("binance"));
    Assert.assertTrue(exchangeService.getExchanges().contains("bitfinex"));
    Assert.assertTrue(exchangeService.getExchanges().contains("bitmex"));
    Assert.assertTrue(exchangeService.getExchanges().contains("kucoin"));
  }

  @Test
  public void testGdaxSandbox() {
    Assert.assertEquals(GDAXStreamingExchange.class, exchangeService.map("gdax-sandbox"));

  }

  @Test
  public void testGdax() {
    Assert.assertEquals(GDAXStreamingExchange.class, exchangeService.map("gdax"));
  }

  @Test
  public void testBitfinex() {
    Assert.assertEquals(BitfinexStreamingExchange.class, exchangeService.map("bitfinex"));
  }

  @Test
  public void testBitmex() {
    Assert.assertEquals(BitmexExchange.class, exchangeService.map("bitmex"));
  }

  @Test
  public void testKucoin() {
    Assert.assertEquals(KucoinExchange.class, exchangeService.map("kucoin"));
  }

  @Test
  public void testBittrex() {
    Assert.assertEquals(BittrexExchange.class, exchangeService.map("bittrex"));
  }
}