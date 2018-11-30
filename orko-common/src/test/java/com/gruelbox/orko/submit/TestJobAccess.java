package com.gruelbox.orko.submit;

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
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterables;
import com.gruelbox.orko.db.DbTesting;
import com.gruelbox.orko.job.DummyJob;
import com.gruelbox.orko.submit.JobAccess.JobAlreadyExistsException;
import com.gruelbox.orko.submit.JobAccess.JobDoesNotExistException;

public class TestJobAccess {

  private JobAccessImpl dao;

  @Before
  public void setup() throws Exception {
    dao = new JobAccessImpl(DbTesting.connectionSource(), new ObjectMapper());
    DbTesting.mutateToSupportSchema(SchemaUtils.schema(dao.tables()));
  }
  
  @Test
  public void testSimpleCrud() throws JobAlreadyExistsException {
    DummyJob job1 = DummyJob.builder().id(UUID.randomUUID().toString()).stringValue("JOBONE").bigDecimalValue(ONE).build();
    DummyJob job2 = DummyJob.builder().id(UUID.randomUUID().toString()).stringValue("JOBTWO").bigDecimalValue(new BigDecimal(2)).build();
    DummyJob job3 = DummyJob.builder().id(UUID.randomUUID().toString()).stringValue("JOBTHREE").bigDecimalValue(new BigDecimal(3)).build();
    
    dao.insert(job1);
    dao.insert(job2);
    assertThat(dao.list(), containsInAnyOrder(job1, job2));
    
    dao.delete(job2.id());
    assertThat(dao.list(), containsInAnyOrder(job1));
    
    dao.insert(job3);
    DummyJob job1Updated = job1.toBuilder().bigDecimalValue(new BigDecimal(123)).build();
    assertNotEquals(job1, job1Updated);
    dao.update(job1Updated);
    assertThat(dao.load(job1.id()), equalTo(job1Updated));
    assertThat(dao.load(job3.id()), equalTo(job3));
    
    dao.deleteAll();
    assertTrue(Iterables.isEmpty(dao.list()));
  }
  
  @Test
  public void testNoReinsertion() throws JobAlreadyExistsException {
    DummyJob job = DummyJob.builder().id(UUID.randomUUID().toString()).stringValue("JOBONE").bigDecimalValue(ONE).build();
    
    dao.insert(job);
    assertThat(dao.list(), containsInAnyOrder(job));
    assertThat(dao.load(job.id()), equalTo(job));
    
    dao.delete(job.id());
    assertTrue(Iterables.isEmpty(dao.list()));
    
    try {
      dao.insert(job);
      fail();
    } catch (JobAlreadyExistsException e) {
      // OK
    }
  }

  @Test
  public void testJobAlreadyExists() throws JobAlreadyExistsException {
    DummyJob job = DummyJob.builder().id(UUID.randomUUID().toString()).stringValue("XXX").bigDecimalValue(ONE).build();
    dao.insert(job);
    try {
      dao.insert(job);
      fail();
    } catch (JobAlreadyExistsException e) {
      // OK
    }
  }
  
  @Test
  public void testUpdateNonExistentJob() {
    try {
      dao.update(DummyJob.builder().id(UUID.randomUUID().toString()).stringValue("XXX").bigDecimalValue(ONE).build());
      fail();
    } catch (JobDoesNotExistException e) {
      // OK
    }
  }
  
  @Test
  public void testLoadNonExistentJob() {
    try {
      dao.load("XXX");
      fail();
    } catch (JobDoesNotExistException e) {
      // OK
    }
  }
  
  @Test
  public void testDeleteNonExistentJob() {
    try {
      dao.delete("XXX");
      fail();
    } catch (JobDoesNotExistException e) {
      // OK
    }
  }
}
