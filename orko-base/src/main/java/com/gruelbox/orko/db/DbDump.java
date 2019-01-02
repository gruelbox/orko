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

/*-
 * ===============================================================================L
 * Orko Base
 * ================================================================================
 * Copyright (C) 2018 - 2019 Graham Crockford
 * ================================================================================
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * ===============================================================================E
 */

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

class DbDump {

  private static final Logger LOGGER = LoggerFactory.getLogger(DbDump.class);

  static File dump(ConnectionResources connectionResources) throws IOException {
    LOGGER.info("Dumping database...");
    File tempFile = File.createTempFile("orko-db-dump-", ".zip");
    new DataSetConnector(
      new DatabaseDataSetProducer(connectionResources),
      new XmlDataSetConsumer(tempFile)
    ).connect();
    LOGGER.info("Database dump complete");
    return tempFile;
  }

  static void restore(String startPositionFile, ConnectionResources connectionResources) {
    LOGGER.info("Restoring database snapshot: {}", startPositionFile);
    try {
      new DataSetConnector(
        new XmlDataSetProducer(Paths.get(startPositionFile).toUri().toURL()),
        new DatabaseDataSetConsumer(connectionResources, new SqlScriptExecutorProvider(connectionResources))
      )
      .connect();
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
    LOGGER.info("Snapshot restored");
  }
}
