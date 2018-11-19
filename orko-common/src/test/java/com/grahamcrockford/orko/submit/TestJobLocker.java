package com.grahamcrockford.orko.submit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.alfasoftware.morf.metadata.SchemaUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.grahamcrockford.orko.OrkoConfiguration;
import com.grahamcrockford.orko.db.DbTesting;

public class TestJobLocker {

  private static final UUID OWNER_1 = UUID.randomUUID();
  private static final UUID OWNER_2 = UUID.randomUUID();
  private static final UUID OWNER_3 = UUID.randomUUID();
  private static final String JOB_1 = "JOB1";
  private static final String JOB_2 = "JOB2";
  private JobLockerImpl dao;

  @Before
  public void setup() throws Exception {
    OrkoConfiguration configuration = new OrkoConfiguration();
    configuration.getDatabase().setLockSeconds(3);
    dao = new JobLockerImpl(configuration, DbTesting.connectionSource());
    DbTesting.mutateToSupportSchema(SchemaUtils.schema(dao.tables()));
    dao.start();
  }
  
  @After
  public void tearDown() throws Exception {
    dao.stop();
  }
  
  @Test
  public void testExpiringLock() throws InterruptedException {
    assertTrue(dao.attemptLock(JOB_1, OWNER_1));
    assertFalse(dao.attemptLock(JOB_1, OWNER_2));
    Thread.sleep(3100);
    dao.cleanup();
    assertTrue(dao.attemptLock(JOB_1, OWNER_2));
  }
  
  @Test
  public void testUpdateLock() throws InterruptedException {
    assertTrue(dao.attemptLock(JOB_1, OWNER_1));
    assertTrue(dao.attemptLock(JOB_2, OWNER_2));
    Thread.sleep(1000);
    dao.cleanup();
    assertTrue(dao.updateLock(JOB_1, OWNER_1));
    Thread.sleep(2100);
    dao.cleanup();
    assertFalse(dao.attemptLock(JOB_1, OWNER_3));
    assertFalse(dao.updateLock(JOB_2, OWNER_2));
    assertTrue(dao.attemptLock(JOB_2, OWNER_3));
  }
  
  @Test
  public void testReleaseAllLocks() {
    assertTrue(dao.attemptLock(JOB_1, OWNER_1));
    assertTrue(dao.attemptLock(JOB_2, OWNER_2));
    dao.releaseAllLocks();
    assertTrue(dao.attemptLock(JOB_1, OWNER_1));
    assertTrue(dao.attemptLock(JOB_2, OWNER_2));
  }
  
  @Test
  public void testReleaseLock() {
    assertTrue(dao.attemptLock(JOB_1, OWNER_1));
    assertTrue(dao.attemptLock(JOB_2, OWNER_2));
    dao.releaseLock(JOB_1, OWNER_1);
    assertTrue(dao.attemptLock(JOB_1, OWNER_1));
    assertFalse(dao.attemptLock(JOB_2, OWNER_2));
  }
  
  @Test
  public void testReleaseAnyLock() {
    assertTrue(dao.attemptLock(JOB_1, OWNER_1));
    assertTrue(dao.attemptLock(JOB_2, OWNER_2));
    dao.releaseAnyLock(JOB_1);
    assertTrue(dao.attemptLock(JOB_1, OWNER_1));
    assertFalse(dao.attemptLock(JOB_2, OWNER_2));
  }
}
