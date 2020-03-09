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

import static org.alfasoftware.morf.metadata.SchemaUtils.schema;
import static org.alfasoftware.morf.upgrade.db.DatabaseUpgradeTableContribution.deployedViewsTable;
import static org.alfasoftware.morf.upgrade.db.DatabaseUpgradeTableContribution.upgradeAuditTable;

import com.google.common.collect.ImmutableList;
import io.dropwizard.testing.junit5.DAOTestExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.alfasoftware.morf.metadata.Schema;
import org.alfasoftware.morf.upgrade.TableContribution;
import org.alfasoftware.morf.upgrade.Upgrade;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(DropwizardExtensionsSupport.class)
public abstract class AbstractTableContributionTest {

  public DAOTestExtension database = DbTesting.extension().build();

  /** Test with initial schema prior to any upgrades. */
  @BeforeEach
  public final void setup() {
    DbTesting.clearDatabase();
    DbTesting.mutateToSupportSchema(
        schema(
            schema(ImmutableList.of(deployedViewsTable(), upgradeAuditTable())), initialSchema()));
  }

  protected abstract Schema initialSchema();

  protected abstract TableContribution tableContribution();

  @AfterEach
  public void tearDown() {
    DbTesting.invalidateSchemaCache();
  }

  /** Ensure that the upgrade chain gets us to our target state. */
  @Test
  public final void testUpgrade() {
    TableContribution contribution = tableContribution();
    Schema targetSchema =
        schema(schema(deployedViewsTable(), upgradeAuditTable()), schema(contribution.tables()));
    Upgrade.performUpgrade(
        targetSchema, contribution.schemaUpgradeClassses(), DbTesting.connectionResources());
  }
}
