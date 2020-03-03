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

import com.google.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Paths;
import org.alfasoftware.morf.dataset.DataSetConnector;
import org.alfasoftware.morf.jdbc.ConnectionResources;
import org.alfasoftware.morf.jdbc.DatabaseDataSetConsumer;
import org.alfasoftware.morf.jdbc.DatabaseDataSetProducer;
import org.alfasoftware.morf.jdbc.SqlScriptExecutorProvider;
import org.alfasoftware.morf.xml.XmlDataSetConsumer;
import org.alfasoftware.morf.xml.XmlDataSetProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbDump {

  private static final Logger LOGGER = LoggerFactory.getLogger(DbDump.class);

  private final ConnectionResources connectionResources;

  @Inject
  public DbDump(ConnectionResources connectionResources) {
    this.connectionResources = connectionResources;
  }

  public File dump() throws IOException {
    LOGGER.info("Dumping database...");
    File tempFile = File.createTempFile("orko-db-dump-", ".zip");
    new DataSetConnector(
            new DatabaseDataSetProducer(connectionResources), new XmlDataSetConsumer(tempFile))
        .connect();
    LOGGER.info("Database dump complete");
    return tempFile;
  }

  public void restore(String startPositionFile) {
    LOGGER.info("Restoring database snapshot: {}", startPositionFile);
    try {
      new DataSetConnector(
              new XmlDataSetProducer(Paths.get(startPositionFile).toUri().toURL()),
              new DatabaseDataSetConsumer(
                  connectionResources, new SqlScriptExecutorProvider(connectionResources)))
          .connect();
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
    LOGGER.info("Snapshot restored");
  }
}
