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

import static java.util.stream.Collectors.toSet;

import com.google.inject.Inject;
import java.util.Set;
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
  private final DbDump dbDump;

  @Inject
  DatabaseSetup(
      Schema targetSchema,
      ConnectionResources connectionResources,
      Set<TableContribution> tableContributions,
      DbConfiguration dbConfiguration,
      DbDump dbDump) {
    this.targetSchema = targetSchema;
    this.connectionResources = connectionResources;
    this.tableContributions = tableContributions;
    this.dbConfiguration = dbConfiguration;
    this.dbDump = dbDump;
  }

  public void setup() {
    if (StringUtils.isNotEmpty(dbConfiguration.getStartPositionFile())) {
      dbDump.restore(dbConfiguration.getStartPositionFile());
    }
    try (SchemaResource currentSchema = connectionResources.openSchemaResource()) {
      Set<Class<? extends UpgradeStep>> upgradeSteps =
          tableContributions.stream()
              .flatMap(c -> c.schemaUpgradeClassses().stream())
              .collect(toSet());
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
