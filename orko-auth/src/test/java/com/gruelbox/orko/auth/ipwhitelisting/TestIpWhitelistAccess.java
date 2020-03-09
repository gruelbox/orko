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
package com.gruelbox.orko.auth.ipwhitelisting;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.inject.util.Providers;
import com.gruelbox.orko.auth.AuthConfiguration;
import com.gruelbox.orko.db.DbTesting;
import com.gruelbox.orko.db.Transactionally;
import io.dropwizard.testing.junit5.DAOTestExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.alfasoftware.morf.metadata.SchemaUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Tag("database")
@ExtendWith(DropwizardExtensionsSupport.class)
public class TestIpWhitelistAccess {

  public DAOTestExtension database =
      DbTesting.extension().addEntityClass(IpWhitelist.class).build();

  private IpWhitelistAccessImpl dao;

  @BeforeEach
  public void setup() throws Exception {
    AuthConfiguration authConfiguration = new AuthConfiguration();
    authConfiguration.setIpWhitelisting(new IpWhitelistingConfiguration());
    authConfiguration.getIpWhitelisting().setWhitelistExpirySeconds(3);
    dao =
        new IpWhitelistAccessImpl(
            Providers.of(database.getSessionFactory()),
            new Transactionally(database.getSessionFactory()),
            authConfiguration);
    DbTesting.clearDatabase();
    DbTesting.invalidateSchemaCache();
    DbTesting.mutateToSupportSchema(SchemaUtils.schema(new IpWhitelistContribution().tables()));
    dao.start();
  }

  @AfterEach
  public void tearDown() throws Exception {
    dao.stop();
  }

  @Test
  public void testAll() throws InterruptedException {
    database.inTransaction(
        () -> {
          dao.add("1");
          dao.add("2");
          dao.add("3");
          dao.delete("2");

          // Second delete in same transaction, should be ignored
          dao.delete("2");

          // Make sure the session is correct
          assertTrue(dao.exists("1"));
          assertFalse(dao.exists("2"));
        });

    // Make sure the changes were flushed
    database.inTransaction(
        () -> {
          assertTrue(dao.exists("1"));
          assertFalse(dao.exists("2"));
        });

    // Second delete in different transaction, should be ignored
    database.inTransaction(() -> dao.delete("2"));

    Thread.sleep(2000);
    database.inTransaction(() -> dao.cleanup());

    database.inTransaction(
        () -> {
          dao.add("4");
        });

    Thread.sleep(1100);
    database.inTransaction(() -> dao.cleanup());

    database.inTransaction(
        () -> {
          assertFalse(dao.exists("1"));
          assertFalse(dao.exists("3"));
          assertTrue(dao.exists("4"));
        });
  }

  @Test
  public void testMerge() throws InterruptedException {
    database.inTransaction(
        () -> {
          dao.add("1");
        });

    database.inTransaction(
        () -> {
          dao.add("1");
        });

    database.inTransaction(
        () -> {
          assertTrue(dao.exists("1"));
          assertFalse(dao.exists("2"));
        });
  }
}
