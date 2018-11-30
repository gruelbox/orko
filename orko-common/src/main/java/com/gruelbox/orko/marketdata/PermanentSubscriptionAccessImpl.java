package com.gruelbox.orko.marketdata;

import static com.gruelbox.orko.spi.TickerSpec.fromKey;
import static java.util.stream.Collectors.toSet;
import static org.alfasoftware.morf.metadata.SchemaUtils.column;
import static org.alfasoftware.morf.metadata.SchemaUtils.table;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.alfasoftware.morf.jdbc.ConnectionResources;
import org.alfasoftware.morf.metadata.DataType;
import org.alfasoftware.morf.metadata.Table;
import org.alfasoftware.morf.sql.SqlUtils;
import org.alfasoftware.morf.upgrade.TableContribution;
import org.alfasoftware.morf.upgrade.UpgradeStep;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.gruelbox.orko.db.ConnectionSource;
import com.gruelbox.orko.spi.TickerSpec;

@Singleton
class PermanentSubscriptionAccessImpl implements PermanentSubscriptionAccess, TableContribution {
  
  private static final Logger LOGGER = LoggerFactory.getLogger(PermanentSubscriptionAccessImpl.class);
  
  private static final String SUBSCRIPTION = "Subscription";
  private static final org.jooq.Table<Record> SUBSCRIPTION_TABLE = DSL.table(SUBSCRIPTION);
  private static final String TICKER = "ticker";
  private static final Field<String> TICKER_FIELD = DSL.field(TICKER, String.class);
  private static final String REFERENCE_PRICE = "referencePrice";
  private static final Field<BigDecimal> REFERENCE_PRICE_FIELD = DSL.field(REFERENCE_PRICE, BigDecimal.class);
  
  private final Supplier<String> mergeStatement;
  private final ConnectionSource connectionSource;

  @Inject
  PermanentSubscriptionAccessImpl(ConnectionSource connectionSource, Provider<ConnectionResources> connectionResources) {
    this.connectionSource = connectionSource;
    
    // Use Morf's DSL for this instead of j00q since Morf's merges work on MySQL too.
    this.mergeStatement = Suppliers.memoize(() -> connectionResources.get().sqlDialect().convertStatementToSQL(
        SqlUtils.merge()
          .into(SqlUtils.tableRef(SUBSCRIPTION))
          .tableUniqueKey(SqlUtils.field(TICKER))
          .from(
            SqlUtils.select(SqlUtils.parameter(TICKER).type(DataType.STRING),
            SqlUtils.parameter(REFERENCE_PRICE).type(DataType.DECIMAL))
          )
    ));
  }

  @Override
  public void add(TickerSpec spec) {
    LOGGER.info("Adding permanent subscription to {}", spec);
    connectionSource.runInTransaction(dsl -> dsl
      .insertInto(SUBSCRIPTION_TABLE).values(spec.key(), null).execute());
  }

  @Override
  public void remove(TickerSpec spec) {
    LOGGER.info("Removing permanent subscription to {}", spec);
    connectionSource.runInTransaction(dsl -> dsl
      .deleteFrom(SUBSCRIPTION_TABLE).where(TICKER_FIELD.eq(spec.key())).execute());
  }

  @Override
  public Set<TickerSpec> all() {
    return connectionSource.getInTransaction(dsl -> dsl
      .select(TICKER_FIELD)
      .from(SUBSCRIPTION_TABLE)
      .fetch()
      .stream()
      .map(r -> fromKey(r.get(TICKER_FIELD)))
      .collect(toSet()));
  }

  @Override
  public void setReferencePrice(TickerSpec tickerSpec, BigDecimal price) {
    connectionSource.runInTransaction(dsl ->  dsl.execute(mergeStatement.get(), tickerSpec.key(), price));
  }

  @Override
  public Map<TickerSpec, BigDecimal> getReferencePrices() {
    return connectionSource.getInTransaction(dsl -> {
      RecordMapper<Record, TickerSpec> keyMapper = r -> fromKey(r.get(TICKER_FIELD));
      RecordMapper<Record, BigDecimal> valueMapper = r -> r.get(REFERENCE_PRICE_FIELD);
      Map<TickerSpec, BigDecimal> unfiltered = dsl
        .select(TICKER_FIELD, REFERENCE_PRICE_FIELD)
        .from(SUBSCRIPTION_TABLE)
        .fetch()
        .intoMap(keyMapper, valueMapper);
      return ImmutableMap.copyOf(Maps.filterValues(unfiltered, v -> v != null));
    });
  }

  @Override
  public Collection<Table> tables() {
    return ImmutableList.of(
      table(SUBSCRIPTION)
        .columns(
          column(TICKER, DataType.STRING, 32).primaryKey(),
          column(REFERENCE_PRICE, DataType.DECIMAL, 13, 8).nullable()
        )
    );
  }

  @Override
  public Collection<Class<? extends UpgradeStep>> schemaUpgradeClassses() {
    return ImmutableList.of();
  }
}