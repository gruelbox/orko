/**
 * Orko
 * Copyright © 2018-2019 Graham Crockford
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
    Multibinder.newSetBinder(binder(), EntityContribution.class);
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
