package com.grahamcrockford.orko.exchange;

import org.junit.Assert;
import org.junit.Test;
import org.knowm.xchange.bittrex.BittrexExchange;
import org.knowm.xchange.cryptopia.CryptopiaExchange;
import org.knowm.xchange.kucoin.KucoinExchange;

import info.bitrich.xchangestream.bitfinex.BitfinexStreamingExchange;
import info.bitrich.xchangestream.gdax.GDAXStreamingExchange;

public class TestExchanges {

  @Test
  public void testGdaxSandbox() {
    Assert.assertEquals(GDAXStreamingExchange.class, Exchanges.friendlyNameToClass(Exchanges.GDAX_SANDBOX));
  }

  @Test
  public void testGdax() {
    Assert.assertEquals(GDAXStreamingExchange.class, Exchanges.friendlyNameToClass(Exchanges.GDAX));
  }

  @Test
  public void testBitfinex() {
    Assert.assertEquals(BitfinexStreamingExchange.class, Exchanges.friendlyNameToClass(Exchanges.BITFINEX));
  }

  @Test
  public void testKucoin() {
    Assert.assertEquals(KucoinExchange.class, Exchanges.friendlyNameToClass(Exchanges.KUCOIN));
  }

  @Test
  public void testBittrex() {
    Assert.assertEquals(BittrexExchange.class, Exchanges.friendlyNameToClass(Exchanges.BITTREX));
  }

  @Test
  public void testCryptopia() {
    Assert.assertEquals(CryptopiaExchange.class, Exchanges.friendlyNameToClass(Exchanges.CRYPTOPIA));
  }
}