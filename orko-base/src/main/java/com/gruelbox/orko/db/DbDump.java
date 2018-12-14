package com.gruelbox.orko.db;

import java.io.File;
import java.io.IOException;

import org.alfasoftware.morf.dataset.DataSetConnector;
import org.alfasoftware.morf.dataset.DataSetConsumer.CloseState;
import org.alfasoftware.morf.jdbc.ConnectionResources;
import org.alfasoftware.morf.jdbc.DatabaseDataSetProducer;
import org.alfasoftware.morf.xml.XmlDataSetConsumer;

class DbDump {

  static File dump(ConnectionResources connectionResources) throws IOException {
    File tempFile = File.createTempFile("orko-db-dump-", ".zip");
    XmlDataSetConsumer consumer = new XmlDataSetConsumer(tempFile);
    try {
      DatabaseDataSetProducer producer = new DatabaseDataSetProducer(connectionResources);
      try {
        new DataSetConnector(producer, consumer).connect();
      } finally {
        producer.close();
      }
    } finally {
      consumer.close(CloseState.COMPLETE);
    }
    return tempFile;
  }

}
