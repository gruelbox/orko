package com.grahamcrockford.orko.exchange;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.knowm.xchange.bittrex.BittrexExchange;
import org.knowm.xchange.kucoin.KucoinExchange;

import com.grahamcrockford.orko.OrkoConfiguration;

import info.bitrich.xchangestream.bitfinex.BitfinexStreamingExchange;
import info.bitrich.xchangestream.gdax.GDAXStreamingExchange;

public class TestExchangeService {

  private ExchangeServiceImpl exchangeService;

  @Before
  public void setup() {
    exchangeService = new ExchangeServiceImpl(new OrkoConfiguration());
  }

  @Test
  public void testExchanges() {
    Assert.assertTrue(exchangeService.getExchanges().contains("gdax"));
    Assert.assertTrue(exchangeService.getExchanges().contains("gdax-sandbox"));
    Assert.assertTrue(exchangeService.getExchanges().contains("binance"));
    Assert.assertTrue(exchangeService.getExchanges().contains("bitfinex"));
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
  public void testKucoin() {
    Assert.assertEquals(KucoinExchange.class, exchangeService.map("kucoin"));
  }

  @Test
  public void testBittrex() {
    Assert.assertEquals(BittrexExchange.class, exchangeService.map("bittrex"));
  }
}