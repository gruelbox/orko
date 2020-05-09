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

import static java.math.BigDecimal.ONE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterables;
import com.google.inject.util.Providers;
import com.gruelbox.orko.db.DbTesting;
import com.gruelbox.orko.jobrun.JobAccess.JobAlreadyExistsException;
import com.gruelbox.orko.jobrun.JobAccess.JobDoesNotExistException;
import io.dropwizard.testing.junit5.DAOTestExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import java.math.BigDecimal;
import java.util.UUID;
import org.alfasoftware.morf.metadata.SchemaUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@Tag("database")
@ExtendWith(DropwizardExtensionsSupport.class)
public class TestJobAccess {

  @Mock private JobLocker jobLocker;

  public DAOTestExtension database = DbTesting.extension().addEntityClass(JobRecord.class).build();

  private JobAccessImpl dao;

  @BeforeEach
  public void setup() throws Exception {
    MockitoAnnotations.initMocks(this);
    dao =
        new JobAccessImpl(
            Providers.of(database.getSessionFactory()), new ObjectMapper(), jobLocker);

    DbTesting.invalidateSchemaCache();
    DbTesting.clearDatabase(); // TODO not sure why this is needed. Looks like the state is getting
    // corrupted somewhere

    DbTesting.mutateToSupportSchema(SchemaUtils.schema(new JobRecordContribution().tables()));
  }

  /**
   * Tests all the basic crud operations, and also flushing semantics (confirming that queries
   * within a session return the same results as afterward).
   */
  @Test
  public void testSimpleCrud() throws JobAlreadyExistsException {
    DummyJob job1 =
        DummyJob.builder()
            .id(UUID.randomUUID().toString())
            .stringValue("JOBONE")
            .bigDecimalValue(ONE)
            .build();
    DummyJob job2 =
        DummyJob.builder()
            .id(UUID.randomUUID().toString())
            .stringValue("JOBTWO")
            .bigDecimalValue(new BigDecimal(2))
            .build();
    DummyJob job3 =
        DummyJob.builder()
            .id(UUID.randomUUID().toString())
            .stringValue("JOBTHREE")
            .bigDecimalValue(new BigDecimal(3))
            .build();

    database.inTransaction(
        () -> {
          dao.insert(job1);
          dao.insert(job2);
          assertThat(dao.list(), containsInAnyOrder(job1, job2));
          return null;
        });

    // Force an evict so we can make sure we're picking up the data from the database next
    database.getSessionFactory().getCurrentSession().clear();

    database.inTransaction(() -> assertThat(dao.list(), containsInAnyOrder(job1, job2)));

    database.inTransaction(
        () -> {
          dao.delete(job2.id());
          assertThat(dao.list(), containsInAnyOrder(job1));
        });
    verify(jobLocker).releaseAnyLock(job2.id());

    database.inTransaction(() -> assertThat(dao.list(), containsInAnyOrder(job1)));

    DummyJob job1Updated = job1.toBuilder().bigDecimalValue(new BigDecimal(123)).build();
    assertNotEquals(job1, job1Updated);

    database.inTransaction(
        () -> {
          dao.insert(job3);
          dao.update(job1Updated);
          assertThat(dao.load(job1.id()), equalTo(job1Updated));
          assertThat(dao.load(job3.id()), equalTo(job3));
          return null;
        });

    database.inTransaction(() -> assertThat(dao.load(job1.id()), equalTo(job1Updated)));
    database.inTransaction(() -> assertThat(dao.load(job3.id()), equalTo(job3)));

    database.inTransaction(
        () -> {
          dao.deleteAll();
          assertTrue(Iterables.isEmpty(dao.list()));
        });

    database.inTransaction(() -> assertTrue(Iterables.isEmpty(dao.list())));
  }

  @Test
  public void testNoReinsertion() throws Exception {
    DummyJob job =
        DummyJob.builder()
            .id(UUID.randomUUID().toString())
            .stringValue("JOBONE")
            .bigDecimalValue(ONE)
            .build();

    database.inTransaction(
        () -> {
          dao.insert(job);
          return null;
        });

    database.inTransaction(
        () -> {
          assertThat(dao.list(), containsInAnyOrder(job));
          assertThat(dao.load(job.id()), equalTo(job));
          dao.delete(job.id());
        });

    database.inTransaction(() -> assertTrue(Iterables.isEmpty(dao.list())));

    try {
      database.inTransaction(
          () -> {
            dao.insert(job);
            return null;
          });
      fail();
    } catch (RuntimeException e) {
      assertTrue(e.getCause() instanceof JobAlreadyExistsException);
    }
  }

  @Test
  public void testJobAlreadyExists() throws Exception {
    DummyJob job =
        DummyJob.builder()
            .id(UUID.randomUUID().toString())
            .stringValue("XXX")
            .bigDecimalValue(ONE)
            .build();

    database.inTransaction(
        () -> {
          dao.insert(job);
          return null;
        });

    try {
      database.inTransaction(
          () -> {
            dao.insert(job);
            return null;
          });
      fail();
    } catch (RuntimeException e) {
      assertTrue(
          e.getCause() instanceof JobAlreadyExistsException,
          "Exception is a " + e.getClass().getName());
    }
  }

  @Test
  public void testUpdateNonExistentJob() {
    try {
      database.inTransaction(
          () ->
              dao.update(
                  DummyJob.builder()
                      .id(UUID.randomUUID().toString())
                      .stringValue("XXX")
                      .bigDecimalValue(ONE)
                      .build()));
      fail();
    } catch (JobDoesNotExistException e) {
      // OK
    }
  }

  @Test
  public void testLoadNonExistentJob() {
    try {
      database.inTransaction(() -> dao.load("XXX"));
      fail();
    } catch (JobDoesNotExistException e) {
      // OK
    }
  }

  @Test
  public void testDeleteNonExistentJob() {
    try {
      database.inTransaction(() -> dao.delete("XXX"));
      fail();
    } catch (JobDoesNotExistException e) {
      // OK
    }
  }
}
