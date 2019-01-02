package com.gruelbox.orko.job.script;

/*-
 * ===============================================================================L
 * Orko Common
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
          column(ScriptParameter.DEFAULT_VALUE, STRING, 255).nullable(),
          column(ScriptParameter.MANDATORY, BOOLEAN)
        )
    );
  }

  @Override
  public Collection<Class<? extends UpgradeStep>> schemaUpgradeClassses() {
    return ImmutableList.of(UpgradeCreateScript.class, UpgradeNullableDefaultValue.class);
  }
}
