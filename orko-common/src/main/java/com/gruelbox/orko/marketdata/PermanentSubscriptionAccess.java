package com.gruelbox.orko.marketdata;

import static com.gruelbox.orko.marketdata.PermanentSubscription.REFERENCE_PRICE;
import static com.gruelbox.orko.marketdata.PermanentSubscription.TABLE_NAME;
import static com.gruelbox.orko.marketdata.PermanentSubscription.TICKER;
import static org.alfasoftware.morf.metadata.DataType.DECIMAL;
import static org.alfasoftware.morf.metadata.DataType.STRING;
import static org.alfasoftware.morf.metadata.SchemaUtils.column;
import static org.alfasoftware.morf.metadata.SchemaUtils.table;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfasoftware.morf.metadata.Table;
import org.alfasoftware.morf.upgrade.TableContribution;
import org.alfasoftware.morf.upgrade.UpgradeStep;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.gruelbox.orko.db.EntityContribution;
import com.gruelbox.orko.spi.TickerSpec;

@Singleton
class PermanentSubscriptionAccess implements TableContribution, EntityContribution {

  private static final Logger LOGGER = LoggerFactory.getLogger(PermanentSubscriptionAccess.class);

  private final Provider<SessionFactory> sessionFactory;

  @Inject
  PermanentSubscriptionAccess(Provider<SessionFactory> sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  void add(TickerSpec spec) {
    LOGGER.info("Adding permanent subscription to {}", spec);
    PermanentSubscription sub = session().get(PermanentSubscription.class, spec.key());
    if (sub == null) {
      session().merge(new PermanentSubscription(spec.key(), null));
    }
  }

  void remove(TickerSpec spec) {
    LOGGER.info("Removing permanent subscription to {}", spec);
    session()
      .createQuery("delete from " + TABLE_NAME + " where id = :id")
      .setParameter("id", spec.key())
      .executeUpdate();
  }

  Set<TickerSpec> all() {
    List<PermanentSubscription> results = session().createQuery("from " + TABLE_NAME, PermanentSubscription.class).list();
    return FluentIterable.from(results).transform(s -> TickerSpec.fromKey(s.ticker)).toSet();
  }

  void setReferencePrice(TickerSpec tickerSpec, BigDecimal price) {
    session().merge(new PermanentSubscription(tickerSpec.key(), price));
  }

  Map<TickerSpec, BigDecimal> getReferencePrices() {
    List<PermanentSubscription> results = session().createQuery("from " + TABLE_NAME, PermanentSubscription.class).list();
    Builder<TickerSpec, BigDecimal> builder = ImmutableMap.builder();
    results.stream()
      .filter(r -> r.referencePrice != null)
      .forEach(r -> builder.put(TickerSpec.fromKey(r.ticker), r.referencePrice));
    return builder.build();
  }

  @Override
  public Collection<Table> tables() {
    return ImmutableList.of(
      table(TABLE_NAME)
        .columns(
          column(TICKER, STRING, 32).primaryKey(),
          column(REFERENCE_PRICE, DECIMAL, 13, 8).nullable()
        )
    );
  }

  @Override
  public Collection<Class<? extends UpgradeStep>> schemaUpgradeClassses() {
    return ImmutableList.of();
  }

  @Override
  public Iterable<Class<?>> getEntities() {
    return ImmutableList.of(PermanentSubscription.class);
  }

  private Session session() {
    return sessionFactory.get().getCurrentSession();
  }
}