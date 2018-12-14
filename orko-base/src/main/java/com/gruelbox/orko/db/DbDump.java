package com.gruelbox.orko.db;

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