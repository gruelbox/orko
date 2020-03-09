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

import static com.gruelbox.orko.jobrun.JobRecord.PROCESSED_FIELD;
import static org.alfasoftware.morf.metadata.SchemaUtils.column;
import static org.alfasoftware.morf.metadata.SchemaUtils.index;
import static org.alfasoftware.morf.metadata.SchemaUtils.schema;
import static org.alfasoftware.morf.metadata.SchemaUtils.table;

import com.gruelbox.orko.db.AbstractTableContributionTest;
import org.alfasoftware.morf.metadata.DataType;
import org.alfasoftware.morf.metadata.Schema;
import org.alfasoftware.morf.upgrade.TableContribution;
import org.junit.jupiter.api.Tag;

@Tag("database")
public class TestJobRecordContribution extends AbstractTableContributionTest {

  @Override
  protected Schema initialSchema() {
    return schema(
        table("Job")
            .columns(
                column("id", DataType.STRING, 45).primaryKey(),
                column("content", DataType.CLOB).nullable(),
                column("processed", DataType.BOOLEAN))
            .indexes(index("Job_1").columns(PROCESSED_FIELD)));
  }

  @Override
  protected TableContribution tableContribution() {
    return new JobRecordContribution();
  }
}
