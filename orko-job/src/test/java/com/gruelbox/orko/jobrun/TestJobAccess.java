package com.gruelbox.orko.jobrun;

import static java.math.BigDecimal.ONE;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.UUID;

import org.alfasoftware.morf.metadata.SchemaUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterables;
import com.google.inject.util.Providers;
import com.gruelbox.orko.db.DbTesting;
import com.gruelbox.orko.db.Transactionally;
import com.gruelbox.orko.jobrun.JobAccess.JobAlreadyExistsException;
import com.gruelbox.orko.jobrun.JobAccess.JobDoesNotExistException;

import io.dropwizard.testing.junit.DAOTestRule;

public class TestJobAccess {

  @Rule
  public DAOTestRule database = DbTesting.rule()
    .addEntityClass(JobRecord.class)
    .build();

  private JobAccessImpl dao;
  private Transactionally transactionally;

  @Before
  public void setup() throws Exception {
    dao = new JobAccessImpl(Providers.of(database.getSessionFactory()), new ObjectMapper());
    transactionally = new Transactionally(database.getSessionFactory());
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

    transactionally.call(() -> {
      dao.insert(job1);
      dao.insert(job2);
      assertThat(dao.list(), containsInAnyOrder(job1, job2));
      return null;
    });

    transactionally.run(() -> assertThat(dao.list(), containsInAnyOrder(job1, job2)));

    transactionally.run(() -> {
      dao.delete(job2.id());
      assertThat(dao.list(), containsInAnyOrder(job1));
    });

    transactionally.run(() -> assertThat(dao.list(), containsInAnyOrder(job1)));

    DummyJob job1Updated = job1.toBuilder().bigDecimalValue(new BigDecimal(123)).build();
    assertNotEquals(job1, job1Updated);

    transactionally.call(() -> {
      dao.insert(job3);
      dao.update(job1Updated);
      assertThat(dao.load(job1.id()), equalTo(job1Updated));
      assertThat(dao.load(job3.id()), equalTo(job3));
      return null;
    });

    transactionally.run(() -> assertThat(dao.load(job1.id()), equalTo(job1Updated)));
    transactionally.run(() -> assertThat(dao.load(job3.id()), equalTo(job3)));

    transactionally.run(() -> {
      dao.deleteAll();
      assertTrue(Iterables.isEmpty(dao.list()));
    });

    transactionally.run(() -> assertTrue(Iterables.isEmpty(dao.list())));
  }

  @Test
  public void testNoReinsertion() throws Exception {
    DummyJob job = DummyJob.builder().id(UUID.randomUUID().toString()).stringValue("JOBONE").bigDecimalValue(ONE).build();

    transactionally.call(() -> {
      dao.insert(job);
      return null;
    });

    transactionally.run(() -> {
      assertThat(dao.list(), containsInAnyOrder(job));
      assertThat(dao.load(job.id()), equalTo(job));
      dao.delete(job.id());
    });

    transactionally.run(() -> assertTrue(Iterables.isEmpty(dao.list())));

    try {
      transactionally.callChecked(() -> {
        dao.insert(job);
        return null;
      });
      fail();
    } catch (JobAlreadyExistsException e) {
      // OK
    }
  }

  @Test
  public void testJobAlreadyExists() throws Exception {
    DummyJob job = DummyJob.builder().id(UUID.randomUUID().toString()).stringValue("XXX").bigDecimalValue(ONE).build();

    transactionally.call(() -> {
      dao.insert(job);
      return null;
    });

    try {
      transactionally.callChecked(() -> {
        dao.insert(job);
        return null;
      });
      fail();
    } catch (JobAlreadyExistsException e) {
      // OK
    }
  }

  @Test
  public void testUpdateNonExistentJob() {
    try {
      transactionally.run(() -> dao.update(DummyJob.builder().id(UUID.randomUUID().toString()).stringValue("XXX").bigDecimalValue(ONE).build()));
      fail();
    } catch (JobDoesNotExistException e) {
      // OK
    }
  }

  @Test
  public void testLoadNonExistentJob() {
    try {
      transactionally.run(() -> dao.load("XXX"));
      fail();
    } catch (JobDoesNotExistException e) {
      // OK
    }
  }

  @Test
  public void testDeleteNonExistentJob() {
    try {
      transactionally.run(() -> dao.delete("XXX"));
      fail();
    } catch (JobDoesNotExistException e) {
      // OK
    }
  }
}
