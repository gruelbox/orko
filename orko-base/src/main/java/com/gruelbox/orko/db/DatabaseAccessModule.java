/**
 * Orko - Copyright © 2018-2019 Graham Crockford
 *
 * <p>This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * <p>You should have received a copy of the GNU Affero General Public License along with this
 * program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.gruelbox.orko.db;

import static org.alfasoftware.morf.upgrade.db.DatabaseUpgradeTableContribution.deployedViewsTable;
import static org.alfasoftware.morf.upgrade.db.DatabaseUpgradeTableContribution.upgradeAuditTable;

import com.google.common.collect.FluentIterable;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import java.util.Set;
import javax.sql.DataSource;
import org.alfasoftware.morf.jdbc.ConnectionResources;
import org.alfasoftware.morf.jdbc.SqlDialect;
import org.alfasoftware.morf.metadata.Schema;
import org.alfasoftware.morf.metadata.SchemaUtils;
import org.alfasoftware.morf.metadata.View;
import org.alfasoftware.morf.upgrade.TableContribution;
import org.alfasoftware.morf.upgrade.UpgradeStep;

class DatabaseAccessModule extends AbstractModule {

  @Override
  protected void configure() {
    Multibinder.newSetBinder(binder(), UpgradeStep.class);
    Multibinder.newSetBinder(binder(), TableContribution.class);
    Multibinder.newSetBinder(binder(), View.class);
    requestInjection(new DbInit());
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
        SchemaUtils.schema(
            FluentIterable.from(contributions).transformAndConcat(TableContribution::tables)),
        SchemaUtils.schema(views));
  }

  @Provides
  SqlDialect sqlDialect(ConnectionResources connectionResources) {
    return connectionResources.sqlDialect();
  }

  @Provides
  DataSource dataSource(ConnectionResources connectionResources) {
    return connectionResources.getDataSource();
  }

  /** Performs database initialisation once the injector has been successfully constructed. */
  private static final class DbInit {
    @Inject
    void start(DatabaseSetup databaseSetup) {
      databaseSetup.setup();
    }
  }
}
