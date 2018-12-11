package com.gruelbox.orko.subscription;

import static com.gruelbox.orko.subscription.Subscription.REFERENCE_PRICE;
import static com.gruelbox.orko.subscription.Subscription.TABLE_NAME;
import static com.gruelbox.orko.subscription.Subscription.TICKER;
import static org.alfasoftware.morf.metadata.DataType.DECIMAL;
import static org.alfasoftware.morf.metadata.DataType.STRING;
import static org.alfasoftware.morf.metadata.SchemaUtils.column;
import static org.alfasoftware.morf.metadata.SchemaUtils.table;

import java.util.Collection;

import org.alfasoftware.morf.metadata.Table;
import org.alfasoftware.morf.upgrade.TableContribution;
import org.alfasoftware.morf.upgrade.UpgradeStep;

import com.google.common.collect.ImmutableList;
import com.google.inject.Singleton;
import com.gruelbox.orko.db.EntityContribution;

@Singleton
class SubscriptionContribution implements TableContribution, EntityContribution {

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
    return ImmutableList.of(Subscription.class);
  }
}