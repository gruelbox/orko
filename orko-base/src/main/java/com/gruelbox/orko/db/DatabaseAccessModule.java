package com.gruelbox.orko.db;

import static org.alfasoftware.morf.upgrade.db.DatabaseUpgradeTableContribution.deployedViewsTable;
import static org.alfasoftware.morf.upgrade.db.DatabaseUpgradeTableContribution.upgradeAuditTable;

import java.util.Set;

import org.alfasoftware.morf.jdbc.ConnectionResources;
import org.alfasoftware.morf.metadata.Schema;
import org.alfasoftware.morf.metadata.SchemaUtils;
import org.alfasoftware.morf.metadata.View;
import org.alfasoftware.morf.upgrade.TableContribution;
import org.alfasoftware.morf.upgrade.UpgradeStep;

import com.google.common.collect.FluentIterable;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;

public class DatabaseAccessModule extends AbstractModule {

  @Override
  protected void configure() {
    Multibinder.newSetBinder(binder(), UpgradeStep.class);
    Multibinder.newSetBinder(binder(), TableContribution.class);
    Multibinder.newSetBinder(binder(), View.class);
  }

  @Provides
  @Singleton
  ConnectionResources connectionResources(DbConfiguration dbConfiguration) {
    return dbConfiguration.toConnectionResources();
  }

  @Provides
  @Singleton
  Schema schema(Set<TableContribution> contributions, Set<View> views) {
    return SchemaUtils.schema(
      SchemaUtils.schema(deployedViewsTable(), upgradeAuditTable()),
      SchemaUtils.schema(FluentIterable.from(contributions).transformAndConcat(TableContribution::tables)),
      SchemaUtils.schema(views)
    );
  }
}