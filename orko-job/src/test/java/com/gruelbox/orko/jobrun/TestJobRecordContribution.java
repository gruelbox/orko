package com.gruelbox.orko.jobrun;

import static com.gruelbox.orko.jobrun.JobRecord.PROCESSED;
import static org.alfasoftware.morf.metadata.SchemaUtils.column;
import static org.alfasoftware.morf.metadata.SchemaUtils.index;
import static org.alfasoftware.morf.metadata.SchemaUtils.schema;
import static org.alfasoftware.morf.metadata.SchemaUtils.table;

import org.alfasoftware.morf.metadata.DataType;
import org.alfasoftware.morf.metadata.Schema;
import org.alfasoftware.morf.upgrade.TableContribution;

import com.gruelbox.orko.db.AbstractTableContributionTest;

public class TestJobRecordContribution extends AbstractTableContributionTest {

  @Override
  protected Schema initialSchema() {
    return schema(
      table("Job")
      .columns(
        column("id", DataType.STRING, 45).primaryKey(),
        column("content", DataType.CLOB).nullable(),
        column("processed", DataType.BOOLEAN)
      )
      .indexes(
        index("Job_1").columns(PROCESSED)
      )
    );
  }

  @Override
  protected TableContribution tableContribution() {
    return new JobRecordContribution();
  }
}