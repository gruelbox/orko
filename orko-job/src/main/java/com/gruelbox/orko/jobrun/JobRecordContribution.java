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

package com.gruelbox.orko.jobrun;


import static com.gruelbox.orko.jobrun.JobRecord.CONTENT;
import static com.gruelbox.orko.jobrun.JobRecord.ID;
import static com.gruelbox.orko.jobrun.JobRecord.PROCESSED;
import static com.gruelbox.orko.jobrun.JobRecord.TABLE_NAME;
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
import com.gruelbox.orko.db.EntityContribution;

@Singleton
class JobRecordContribution implements TableContribution, EntityContribution {

  @Override
  public Collection<Table> tables() {
    return ImmutableList.of(
      table(TABLE_NAME)
        .columns(
          column(ID, DataType.STRING, 45).primaryKey(),
          column(CONTENT, DataType.CLOB).nullable(),
          column(PROCESSED, DataType.BOOLEAN)
        )
        .indexes(
          index(TABLE_NAME + "_1").columns(PROCESSED)
        )
    );
  }

  @Override
  public Collection<Class<? extends UpgradeStep>> schemaUpgradeClassses() {
    return ImmutableList.of();
  }

  @Override
  public Iterable<Class<?>> getEntities() {
    return ImmutableList.of(JobRecord.class);
  }
}
