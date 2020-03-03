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

import static com.gruelbox.orko.subscription.Subscription.REFERENCE_PRICE_FIELD;
import static com.gruelbox.orko.subscription.Subscription.TABLE_NAME;
import static com.gruelbox.orko.subscription.Subscription.TICKER_FIELD;
import static org.alfasoftware.morf.metadata.DataType.DECIMAL;
import static org.alfasoftware.morf.metadata.DataType.STRING;
import static org.alfasoftware.morf.metadata.SchemaUtils.column;
import static org.alfasoftware.morf.metadata.SchemaUtils.table;

import com.google.common.collect.ImmutableList;
import com.google.inject.Singleton;
import com.gruelbox.tools.dropwizard.guice.hibernate.EntityContribution;
import java.util.Collection;
import org.alfasoftware.morf.metadata.Table;
import org.alfasoftware.morf.upgrade.TableContribution;
import org.alfasoftware.morf.upgrade.UpgradeStep;

@Singleton
class SubscriptionContribution implements TableContribution, EntityContribution {

  @Override
  public Collection<Table> tables() {
    return ImmutableList.of(
        table(TABLE_NAME)
            .columns(
                column(TICKER_FIELD, STRING, 32).primaryKey(),
                column(REFERENCE_PRICE_FIELD, DECIMAL, 13, 8).nullable()));
  }

  @Override
  public Collection<Class<? extends UpgradeStep>> schemaUpgradeClassses() {
    return ImmutableList.of();
  }

  @Override
  public Iterable<Class<?>> getEntities() {
    return ImmutableList.of(Subscription.class);
  }
}
