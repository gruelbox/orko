package com.gruelbox.orko.jobrun;

import static org.alfasoftware.morf.metadata.SchemaUtils.column;
import static org.alfasoftware.morf.metadata.SchemaUtils.index;
import static org.alfasoftware.morf.metadata.SchemaUtils.table;

import java.util.Collection;

import org.alfasoftware.morf.metadata.DataType;
import org.alfasoftware.morf.metadata.Table;
import org.alfasoftware.morf.upgrade.TableContribution;
import org.alfasoftware.morf.upgrade.UpgradeStep;

import com.google.common.collect.ImmutableList;
import com.google.inject.Singleton;

@Singleton
class JobLockContribution implements TableContribution {

  static final String TABLE_NAME = "JobLock";
  static final String JOB_ID = "jobId";
  static final String OWNER_ID = "ownerId";
  static final String EXPIRES = "expires";

  @Override
  public Collection<Table> tables() {
    return ImmutableList.of(
        table(TABLE_NAME)
          .columns(
            column(JOB_ID, DataType.STRING, 45).primaryKey(),
            column(OWNER_ID, DataType.STRING, 255),
            column(EXPIRES, DataType.BIG_INTEGER)
          )
          .indexes(
            index(TABLE_NAME + "_1").columns(OWNER_ID)
          )
      );
  }

  @Override
  public Collection<Class<? extends UpgradeStep>> schemaUpgradeClassses() {
    return ImmutableList.of();
  }
}