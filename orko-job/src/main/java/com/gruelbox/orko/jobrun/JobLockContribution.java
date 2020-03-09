/**
 * Orko - Copyright © 2018-2019 Graham Crockford
 *
 * <p>This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * <p>You should have received a copy of the GNU Affero General Public License along with this
 * program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.gruelbox.orko.jobrun;

import static org.alfasoftware.morf.metadata.SchemaUtils.column;
import static org.alfasoftware.morf.metadata.SchemaUtils.index;
import static org.alfasoftware.morf.metadata.SchemaUtils.table;

import com.google.common.collect.ImmutableList;
import com.google.inject.Singleton;
import java.util.Collection;
import org.alfasoftware.morf.metadata.DataType;
import org.alfasoftware.morf.metadata.Table;
import org.alfasoftware.morf.upgrade.TableContribution;
import org.alfasoftware.morf.upgrade.UpgradeStep;

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
                column(EXPIRES, DataType.BIG_INTEGER))
            .indexes(index(TABLE_NAME + "_1").columns(OWNER_ID)));
  }

  @Override
  public Collection<Class<? extends UpgradeStep>> schemaUpgradeClassses() {
    return ImmutableList.of();
  }
}
