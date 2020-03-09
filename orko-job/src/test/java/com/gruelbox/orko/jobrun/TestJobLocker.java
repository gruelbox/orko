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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gruelbox.orko.db.DbTesting;
import com.gruelbox.orko.db.Transactionally;
import com.gruelbox.orko.jobrun.spi.JobRunConfiguration;
import io.dropwizard.testing.junit5.DAOTestExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import java.time.LocalDateTime;
import java.util.UUID;
import org.alfasoftware.morf.metadata.SchemaUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Tag("database")
@ExtendWith(DropwizardExtensionsSupport.class)
public class TestJobLocker {

  public DAOTestExtension database = DbTesting.extension().build();

  private static final UUID OWNER_1 = UUID.randomUUID();
  private static final UUID OWNER_2 = UUID.randomUUID();
  private static final UUID OWNER_3 = UUID.randomUUID();
  private static final String JOB_1 = "JOB1";
  private static final String JOB_2 = "JOB2";
  private JobLockerImpl dao;

  @BeforeEach
  public void setup() throws Exception {
    JobRunConfiguration configuration = new JobRunConfiguration();
    configuration.setDatabaseLockSeconds(3);
    dao =
        new JobLockerImpl(
            configuration,
            DbTesting.connectionSource(database.getSessionFactory()),
            new Transactionally(database.getSessionFactory()));

    DbTesting.clearDatabase();
    DbTesting.invalidateSchemaCache();
    DbTesting.mutateToSupportSchema(SchemaUtils.schema(new JobLockContribution().tables()));
  }

  @Test
  public void testExpiringLock() throws InterruptedException {
    LocalDateTime time = LocalDateTime.now();

    assertTrue(dao.attemptLock(JOB_1, OWNER_1, time));

    time = time.plusSeconds(2);
    dao.cleanup(time);

    assertFalse(dao.attemptLock(JOB_1, OWNER_2, time));

    time = time.plusSeconds(1);
    dao.cleanup(time);

    assertTrue(dao.attemptLock(JOB_1, OWNER_2, time));
  }

  @Test
  public void testUpdateLock() throws InterruptedException {
    LocalDateTime time = LocalDateTime.now();

    assertTrue(dao.attemptLock(JOB_1, OWNER_1, time));
    assertTrue(dao.attemptLock(JOB_2, OWNER_2, time));

    time = time.plusSeconds(1);
    dao.cleanup(time);

    assertTrue(dao.updateLock(JOB_1, OWNER_1, time));

    time = time.plusSeconds(2);
    dao.cleanup(time);

    assertFalse(dao.attemptLock(JOB_1, OWNER_3, time));
    assertFalse(dao.updateLock(JOB_2, OWNER_2, time));
    assertTrue(dao.attemptLock(JOB_2, OWNER_3, time));
  }

  @Test
  public void testReleaseAllLocks() {
    LocalDateTime time = LocalDateTime.now();

    assertTrue(dao.attemptLock(JOB_1, OWNER_1, time));
    assertTrue(dao.attemptLock(JOB_2, OWNER_2, time));
    dao.releaseAllLocks();
    assertTrue(dao.attemptLock(JOB_1, OWNER_1, time));
    assertTrue(dao.attemptLock(JOB_2, OWNER_2, time));
  }

  @Test
  public void testReleaseLock() {
    LocalDateTime time = LocalDateTime.now();

    assertTrue(dao.attemptLock(JOB_1, OWNER_1, time));
    assertTrue(dao.attemptLock(JOB_2, OWNER_2, time));
    dao.releaseLock(JOB_1, OWNER_1);
    assertTrue(dao.attemptLock(JOB_1, OWNER_1, time));
    assertFalse(dao.attemptLock(JOB_2, OWNER_2, time));
  }

  @Test
  public void testReleaseAnyLock() {
    LocalDateTime time = LocalDateTime.now();

    assertTrue(dao.attemptLock(JOB_1, OWNER_1, time));
    assertTrue(dao.attemptLock(JOB_2, OWNER_2, time));
    dao.releaseAnyLock(JOB_1);
    assertTrue(dao.attemptLock(JOB_1, OWNER_1, time));
    assertFalse(dao.attemptLock(JOB_2, OWNER_2, time));
  }
}
