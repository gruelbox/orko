package com.grahamcrockford.orko.marketdata;

import static java.math.BigDecimal.ONE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Map;

import org.alfasoftware.morf.metadata.SchemaUtils;
import org.junit.Before;
import org.junit.Test;

import com.google.inject.util.Providers;
import com.grahamcrockford.orko.db.DbTesting;
import com.grahamcrockford.orko.marketdata.PermanentSubscriptionAccessImpl;
import com.grahamcrockford.orko.spi.TickerSpec;

public class TestPermanentSubscriptionAccess {
  
  private static final TickerSpec TICKER_1 = TickerSpec.builder().exchange("foo").base("XX1").counter("YYYYY").build();
  private static final TickerSpec TICKER_2 = TickerSpec.builder().exchange("foo").base("XX2").counter("YYYYY").build();
  private static final TickerSpec TICKER_3 = TickerSpec.builder().exchange("foo").base("XX3").counter("YYYYY").build();
  
  private PermanentSubscriptionAccessImpl dao;

  @Before
  public void setup() {
    dao = new PermanentSubscriptionAccessImpl(DbTesting.connectionSource(), Providers.of(DbTesting.connectionResources()));
    DbTesting.mutateToSupportSchema(SchemaUtils.schema(dao.tables()));
  }
  
  @Test
  public void testAll() {
    dao.add(TICKER_1);
    dao.add(TICKER_2);
    dao.add(TICKER_3);
    dao.remove(TICKER_2);
    dao.setReferencePrice(TICKER_3, ONE);
    assertThat(dao.all(), containsInAnyOrder(TICKER_1, TICKER_3));
    Map<TickerSpec, BigDecimal> referencePrices = dao.getReferencePrices();
    assertTrue(referencePrices.get(TICKER_3).compareTo(ONE) == 0);
  }
}