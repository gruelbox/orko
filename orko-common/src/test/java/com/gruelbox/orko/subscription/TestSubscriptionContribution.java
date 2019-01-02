package com.gruelbox.orko.subscription;

import static org.alfasoftware.morf.metadata.DataType.DECIMAL;
import static org.alfasoftware.morf.metadata.DataType.STRING;
import static org.alfasoftware.morf.metadata.SchemaUtils.column;
import static org.alfasoftware.morf.metadata.SchemaUtils.schema;
import static org.alfasoftware.morf.metadata.SchemaUtils.table;

import org.alfasoftware.morf.metadata.Schema;
import org.alfasoftware.morf.upgrade.TableContribution;

import com.gruelbox.orko.db.AbstractTableContributionTest;

public class TestSubscriptionContribution extends AbstractTableContributionTest {

  @Override
  protected Schema initialSchema() {
    return schema(
      table("Subscription")
      .columns(
        column("ticker", STRING, 32).primaryKey(),
        column("referencePrice", DECIMAL, 13, 8).nullable()
      )
    );
  }

  @Override
  protected TableContribution tableContribution() {
    return new SubscriptionContribution();
  }
}