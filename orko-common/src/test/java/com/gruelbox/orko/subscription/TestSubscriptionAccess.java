package com.gruelbox.orko.subscription;

import static java.math.BigDecimal.ONE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Map;

import org.alfasoftware.morf.metadata.SchemaUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.google.inject.util.Providers;
import com.gruelbox.orko.db.DbTesting;
import com.gruelbox.orko.db.Transactionally;
import com.gruelbox.orko.spi.TickerSpec;

import io.dropwizard.testing.junit.DAOTestRule;

public class TestSubscriptionAccess {

  @Rule
  public DAOTestRule database = DbTesting.rule()
    .addEntityClass(Subscription.class)
    .build();

  private static final TickerSpec TICKER_1 = TickerSpec.builder().exchange("foo").base("XX1").counter("YYYYY").build();
  private static final TickerSpec TICKER_2 = TickerSpec.builder().exchange("foo").base("XX2").counter("YYYYY").build();
  private static final TickerSpec TICKER_3 = TickerSpec.builder().exchange("foo").base("XX3").counter("YYYYY").build();

  private SubscriptionAccess dao;
  private Transactionally transactionally;

  @Before
  public void setup() {
    dao = new SubscriptionAccess(Providers.of(database.getSessionFactory()));
    DbTesting.mutateToSupportSchema(SchemaUtils.schema(new SubscriptionContribution().tables()));
    transactionally = new Transactionally(database.getSessionFactory());
  }

  @Test
  public void testAll() {
    transactionally.run(() -> {
      dao.add(TICKER_1);
      dao.add(TICKER_2);
      dao.add(TICKER_3);
    });
    transactionally.run(() -> dao.remove(TICKER_2));
    transactionally.run(() -> dao.setReferencePrice(TICKER_3, ONE));
    transactionally.run(() -> {
      assertThat(dao.all(), containsInAnyOrder(TICKER_1, TICKER_3));
      Map<TickerSpec, BigDecimal> referencePrices = dao.getReferencePrices();
      assertTrue(referencePrices.get(TICKER_3).compareTo(ONE) == 0);
    });
  }

  @Test
  public void testReplace() {
    transactionally.run(() -> dao.add(TICKER_1));
    transactionally.run(() -> dao.add(TICKER_1));
    transactionally.run(() -> assertThat(dao.all(), containsInAnyOrder(TICKER_1)));
  }
}