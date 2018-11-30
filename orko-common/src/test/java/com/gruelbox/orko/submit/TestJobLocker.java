package com.gruelbox.orko.submit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.util.UUID;

import org.alfasoftware.morf.metadata.SchemaUtils;
import org.junit.Before;
import org.junit.Test;

import com.gruelbox.orko.OrkoConfiguration;
import com.gruelbox.orko.db.DbTesting;

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
