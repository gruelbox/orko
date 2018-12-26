package com.gruelbox.orko.job.script;

import static org.alfasoftware.morf.metadata.DataType.BOOLEAN;
import static org.alfasoftware.morf.metadata.DataType.CLOB;
import static org.alfasoftware.morf.metadata.DataType.STRING;
import static org.alfasoftware.morf.metadata.SchemaUtils.column;
import static org.alfasoftware.morf.metadata.SchemaUtils.table;

import java.util.Collection;

import org.alfasoftware.morf.metadata.Table;
import org.alfasoftware.morf.upgrade.TableContribution;
import org.alfasoftware.morf.upgrade.UpgradeStep;

import com.google.common.collect.ImmutableList;
import com.gruelbox.orko.db.EntityContribution;

class ScriptContribution implements EntityContribution, TableContribution {

  @Override
  public Iterable<Class<?>> getEntities() {
    return ImmutableList.of(Script.class, ScriptParameter.class);
  }

  @Override
  public Collection<Table> tables() {
    return ImmutableList.of(
      table(Script.TABLE_NAME)
        .columns(
          column(Script.ID, STRING, 45).primaryKey(),
          column(Script.NAME, STRING, 255),
          column(Script.SCRIPT, CLOB),
          column(Script.SCRIPT_HASH, STRING, 255)
        ),
      table(ScriptParameter.TABLE_NAME)
        .columns(
          column(ScriptParameter.SCRIPT_ID, STRING, 45).primaryKey(),
          column(ScriptParameter.NAME, STRING, 255).primaryKey(),
          column(ScriptParameter.DESCRIPTION, STRING, 255),
          column(ScriptParameter.DEFAULT_VALUE, STRING, 255),
          column(ScriptParameter.MANDATORY, BOOLEAN)
        )
    );
  }

  @Override
  public Collection<Class<? extends UpgradeStep>> schemaUpgradeClassses() {
    return ImmutableList.of(UpgradeCreateScript.class);
  }
}