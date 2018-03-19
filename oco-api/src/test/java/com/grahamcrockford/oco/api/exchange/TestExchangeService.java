package com.grahamcrockford.oco.api.exchange;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.knowm.xchange.gdax.GDAXExchange;
import com.grahamcrockford.oco.OcoConfiguration;
import com.grahamcrockford.oco.api.exchange.ExchangeServiceImpl;

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
  }

  @Test
  public void testGdaxSandbox() {
    Assert.assertEquals(GDAXExchange.class, exchangeService.map("gdax-sandbox"));
  }

  @Test
  public void testGdax() {
    Assert.assertEquals(GDAXExchange.class, exchangeService.map("gdax"));
  }
}