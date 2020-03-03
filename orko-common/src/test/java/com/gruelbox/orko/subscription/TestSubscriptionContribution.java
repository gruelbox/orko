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
package com.gruelbox.orko.subscription;

import static org.alfasoftware.morf.metadata.DataType.DECIMAL;
import static org.alfasoftware.morf.metadata.DataType.STRING;
import static org.alfasoftware.morf.metadata.SchemaUtils.column;
import static org.alfasoftware.morf.metadata.SchemaUtils.schema;
import static org.alfasoftware.morf.metadata.SchemaUtils.table;

import com.gruelbox.orko.db.AbstractTableContributionTest;
import org.alfasoftware.morf.metadata.Schema;
import org.alfasoftware.morf.upgrade.TableContribution;
import org.junit.jupiter.api.Tag;

@Tag("database")
public class TestSubscriptionContribution extends AbstractTableContributionTest {

  @Override
  protected Schema initialSchema() {
    return schema(
        table("Subscription")
            .columns(
                column("ticker", STRING, 32).primaryKey(),
                column("referencePrice", DECIMAL, 13, 8).nullable()));
  }

  @Override
  protected TableContribution tableContribution() {
    return new SubscriptionContribution();
  }
}
