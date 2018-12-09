package com.gruelbox.orko.auth.ipwhitelisting;

import static org.alfasoftware.morf.metadata.SchemaUtils.column;
import static org.alfasoftware.morf.metadata.SchemaUtils.index;
import static org.alfasoftware.morf.metadata.SchemaUtils.schema;
import static org.alfasoftware.morf.metadata.SchemaUtils.table;

import org.alfasoftware.morf.metadata.DataType;
import org.alfasoftware.morf.metadata.Schema;
import org.alfasoftware.morf.upgrade.TableContribution;

import com.gruelbox.orko.db.AbstractTableContributionTest;

public class TestIpWhitelistContribution extends AbstractTableContributionTest {

  @Override
  protected Schema initialSchema() {
    return schema(
      table("IpWhitelist")
        .columns(
          column("ip", DataType.STRING, 45).primaryKey(),
          column("expires", DataType.BIG_INTEGER)
        )
        .indexes(
          index("IpWhitelist_1").columns("expires")
        )
    );
  }

  @Override
  protected TableContribution tableContribution() {
    return new IpWhitelistContribution();
  }
}