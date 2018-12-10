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

import com.google.common.collect.ImmutableList;

import io.dropwizard.testing.junit.DAOTestRule;

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