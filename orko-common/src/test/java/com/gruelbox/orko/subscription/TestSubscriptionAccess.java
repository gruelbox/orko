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

import static java.math.BigDecimal.ONE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.inject.util.Providers;
import com.gruelbox.orko.db.DbTesting;
import com.gruelbox.orko.spi.TickerSpec;
import io.dropwizard.testing.junit5.DAOTestExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import java.math.BigDecimal;
import java.util.Map;
import org.alfasoftware.morf.metadata.SchemaUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Tag("database")
@ExtendWith(DropwizardExtensionsSupport.class)
public class TestSubscriptionAccess {

  public DAOTestExtension database =
      DbTesting.extension().addEntityClass(Subscription.class).build();

  private static final TickerSpec TICKER_1 =
      TickerSpec.builder().exchange("foo").base("XX1").counter("YYYYY").build();
  private static final TickerSpec TICKER_2 =
      TickerSpec.builder().exchange("foo").base("XX2").counter("YYYYY").build();
  private static final TickerSpec TICKER_3 =
      TickerSpec.builder().exchange("foo").base("XX3").counter("YYYYY").build();

  private SubscriptionAccess dao;

  @BeforeEach
  public void setup() {
    dao = new SubscriptionAccess(Providers.of(database.getSessionFactory()));
    DbTesting.clearDatabase();
    DbTesting.invalidateSchemaCache();
    DbTesting.mutateToSupportSchema(SchemaUtils.schema(new SubscriptionContribution().tables()));
  }

  @Test
  public void testAll() {
    database.inTransaction(
        () -> {
          dao.add(TICKER_1);
          dao.add(TICKER_2);
          dao.add(TICKER_3);
        });
    database.inTransaction(() -> dao.remove(TICKER_2));
    database.inTransaction(() -> dao.setReferencePrice(TICKER_3, ONE));
    database.inTransaction(
        () -> {
          assertThat(dao.all(), containsInAnyOrder(TICKER_1, TICKER_3));
          Map<TickerSpec, BigDecimal> referencePrices = dao.getReferencePrices();
          assertTrue(referencePrices.get(TICKER_3).compareTo(ONE) == 0);
        });
  }

  @Test
  public void testReplace() {
    database.inTransaction(() -> dao.add(TICKER_1));
    database.inTransaction(() -> dao.add(TICKER_1));
    database.inTransaction(() -> assertThat(dao.all(), containsInAnyOrder(TICKER_1)));
  }
}
