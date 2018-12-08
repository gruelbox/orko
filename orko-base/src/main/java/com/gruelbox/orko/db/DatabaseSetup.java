package com.gruelbox.orko.db;

import static java.util.stream.Collectors.toSet;

import java.util.Set;

import javax.inject.Inject;

import org.alfasoftware.morf.jdbc.ConnectionResources;
import org.alfasoftware.morf.metadata.Schema;
import org.alfasoftware.morf.metadata.SchemaResource;
import org.alfasoftware.morf.upgrade.Deployment;
import org.alfasoftware.morf.upgrade.TableContribution;
import org.alfasoftware.morf.upgrade.Upgrade;
import org.alfasoftware.morf.upgrade.UpgradeStep;

public class DatabaseSetup {

  private final Schema targetSchema;
  private final ConnectionResources connectionResources;
  private final Set<TableContribution> tableContributions;

  @Inject
  DatabaseSetup(Schema targetSchema, ConnectionResources connectionResources, Set<TableContribution> tableContributions) {
    this.targetSchema = targetSchema;
    this.connectionResources = connectionResources;
    this.tableContributions = tableContributions;
  }

  public void setup() {
    try (SchemaResource currentSchema = connectionResources.openSchemaResource()) {
      Set<Class<? extends UpgradeStep>> upgradeSteps = tableContributions.stream().flatMap(c -> c.schemaUpgradeClassses().stream()).collect(toSet());
      if (currentSchema.isEmptyDatabase()) {
        Deployment.deploySchema(targetSchema, upgradeSteps, connectionResources);
      } else {
        Upgrade.performUpgrade(targetSchema, upgradeSteps, connectionResources);
      }
    }
  }
}