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
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseSetup {

  private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseSetup.class);

  private final Schema targetSchema;
  private final ConnectionResources connectionResources;
  private final Set<TableContribution> tableContributions;
  private final DbConfiguration dbConfiguration;

  @Inject
  DatabaseSetup(Schema targetSchema,
                ConnectionResources connectionResources,
                Set<TableContribution> tableContributions,
                DbConfiguration dbConfiguration) {
    this.targetSchema = targetSchema;
    this.connectionResources = connectionResources;
    this.tableContributions = tableContributions;
    this.dbConfiguration = dbConfiguration;
  }

  public void setup() {
    if (StringUtils.isNotEmpty(dbConfiguration.getStartPositionFile())) {
      DbDump.restore(dbConfiguration.getStartPositionFile(), connectionResources);
    }
    try (SchemaResource currentSchema = connectionResources.openSchemaResource()) {
      Set<Class<? extends UpgradeStep>> upgradeSteps = tableContributions.stream().flatMap(c -> c.schemaUpgradeClassses().stream()).collect(toSet());
      if (currentSchema.isEmptyDatabase()) {
        LOGGER.info("Empty database. Deploying schema");
        Deployment.deploySchema(targetSchema, upgradeSteps, connectionResources);
      } else {
        LOGGER.info("Existing database. Checking and upgrading");
        Upgrade.performUpgrade(targetSchema, upgradeSteps, connectionResources);
      }
    }
  }
}