package com.gruelbox.orko.jobrun;

import static java.math.BigDecimal.ONE;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.UUID;

import org.alfasoftware.morf.metadata.SchemaUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterables;
import com.google.inject.util.Providers;
import com.gruelbox.orko.db.DbTesting;
import com.gruelbox.orko.jobrun.JobAccess.JobAlreadyExistsException;
import com.gruelbox.orko.jobrun.JobAccess.JobDoesNotExistException;

import io.dropwizard.testing.junit.DAOTestRule;

public class TestJobAccess {

  @Mock private JobLocker jobLocker;

  @Rule
  public DAOTestRule database = DbTesting.rule()
    .addEntityClass(JobRecord.class)
    .build();

  private JobAccessImpl dao;

  @Before
  public void setup() throws Exception {
    MockitoAnnotations.initMocks(this);
    dao = new JobAccessImpl(Providers.of(database.getSessionFactory()), new ObjectMapper(), jobLocker);
    DbTesting.mutateToSupportSchema(SchemaUtils.schema(new JobRecordContribution().tables()));
  }

  /**
   * Tests all the basic crud operations, and also flushing
   * semantics (confirming that queries within a session return the
   * same results as afterward).
   */
  @Test
  public void testSimpleCrud() throws JobAlreadyExistsException {
    DummyJob job1 = DummyJob.builder().id(UUID.randomUUID().toString()).stringValue("JOBONE").bigDecimalValue(ONE).build();
    DummyJob job2 = DummyJob.builder().id(UUID.randomUUID().toString()).stringValue("JOBTWO").bigDecimalValue(new BigDecimal(2)).build();
    DummyJob job3 = DummyJob.builder().id(UUID.randomUUID().toString()).stringValue("JOBTHREE").bigDecimalValue(new BigDecimal(3)).build();

    database.inTransaction(() -> {
      dao.insert(job1);
      dao.insert(job2);
      assertThat(dao.list(), containsInAnyOrder(job1, job2));
      return null;
    });

    // Force an evict so we can make sure we're picking up the data from the database next
    database.getSessionFactory().getCurrentSession().clear();

    database.inTransaction(() -> assertThat(dao.list(), containsInAnyOrder(job1, job2)));

    database.inTransaction(() -> {
      dao.delete(job2.id());
      assertThat(dao.list(), containsInAnyOrder(job1));
    });
    verify(jobLocker).releaseAnyLock(job2.id());

    database.inTransaction(() -> assertThat(dao.list(), containsInAnyOrder(job1)));

    DummyJob job1Updated = job1.toBuilder().bigDecimalValue(new BigDecimal(123)).build();
    assertNotEquals(job1, job1Updated);

    database.inTransaction(() -> {
      dao.insert(job3);
      dao.update(job1Updated);
      assertThat(dao.load(job1.id()), equalTo(job1Updated));
      assertThat(dao.load(job3.id()), equalTo(job3));
      return null;
    });

    database.inTransaction(() -> assertThat(dao.load(job1.id()), equalTo(job1Updated)));
    database.inTransaction(() -> assertThat(dao.load(job3.id()), equalTo(job3)));

    database.inTransaction(() -> {
      dao.deleteAll();
      assertTrue(Iterables.isEmpty(dao.list()));
    });

    database.inTransaction(() -> assertTrue(Iterables.isEmpty(dao.list())));
  }

  @Test
  public void testNoReinsertion() throws Exception {
    DummyJob job = DummyJob.builder().id(UUID.randomUUID().toString()).stringValue("JOBONE").bigDecimalValue(ONE).build();

    database.inTransaction(() -> {
      dao.insert(job);
      return null;
    });

    database.inTransaction(() -> {
      assertThat(dao.list(), containsInAnyOrder(job));
      assertThat(dao.load(job.id()), equalTo(job));
      dao.delete(job.id());
    });

    database.inTransaction(() -> assertTrue(Iterables.isEmpty(dao.list())));

    try {
      database.inTransaction(() -> {
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
    DummyJob job = DummyJob.builder().id(UUID.randomUUID().toString()).stringValue("XXX").bigDecimalValue(ONE).build();

    database.inTransaction(() -> {
      dao.insert(job);
      return null;
    });

    try {
      database.inTransaction(() -> {
        dao.insert(job);
        return null;
      });
      fail();
    } catch (RuntimeException e) {
      assertTrue("Exception is a " + e.getClass().getName(), e.getCause() instanceof JobAlreadyExistsException);
    }
  }

  @Test
  public void testUpdateNonExistentJob() {
    try {
      database.inTransaction(() -> dao.update(DummyJob.builder().id(UUID.randomUUID().toString()).stringValue("XXX").bigDecimalValue(ONE).build()));
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
