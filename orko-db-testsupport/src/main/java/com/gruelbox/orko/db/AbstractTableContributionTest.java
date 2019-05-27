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


import static org.alfasoftware.morf.metadata.SchemaUtils.schema;
import static org.alfasoftware.morf.upgrade.db.DatabaseUpgradeTableContribution.deployedViewsTable;
import static org.alfasoftware.morf.upgrade.db.DatabaseUpgradeTableContribution.upgradeAuditTable;

import org.alfasoftware.morf.metadata.Schema;
import org.alfasoftware.morf.upgrade.TableContribution;
import org.alfasoftware.morf.upgrade.Upgrade;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.google.common.collect.ImmutableList;

import io.dropwizard.testing.junit.DAOTestRule;

@Category(DatabaseTest.class)
public abstract class AbstractTableContributionTest {

  @Rule
  public DAOTestRule database = DbTesting.rule().build();

  /**
   * Test with initial schema prior to any upgrades.
   */
  @Before
  public final void setup() {
    DbTesting.clearDatabase();
    DbTesting.mutateToSupportSchema(schema(
      schema(ImmutableList.of(deployedViewsTable(), upgradeAuditTable())),
      initialSchema()
    ));
  }

  protected abstract Schema initialSchema();

  protected abstract TableContribution tableContribution();

  @After
  public void tearDown() {
    DbTesting.invalidateSchemaCache();
  }

  /**
   * Ensure that the upgrade chain gets us to our target state.
   */
  @Test
  public final void testUpgrade() {
    TableContribution contribution = tableContribution();
    Schema targetSchema = schema(
      schema(deployedViewsTable(), upgradeAuditTable()),
      schema(contribution.tables())
    );
    Upgrade.performUpgrade(targetSchema, contribution.schemaUpgradeClassses(), DbTesting.connectionResources());
  }
}
