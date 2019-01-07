/**
 * Orko
 * Copyright © 2018-2019 Graham Crockford
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.gruelbox.orko.jobrun;


import static org.alfasoftware.morf.metadata.SchemaUtils.schema;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Injector;
import com.google.inject.util.Providers;
import com.gruelbox.orko.db.DbTesting;
import com.gruelbox.orko.db.Transactionally;
import com.gruelbox.orko.jobrun.TestingJobEvent.EventType;
import com.gruelbox.orko.jobrun.spi.Job;
import com.gruelbox.orko.jobrun.spi.JobControl;
import com.gruelbox.orko.jobrun.spi.JobProcessor;
import com.gruelbox.orko.jobrun.spi.JobRunConfiguration;
import com.gruelbox.orko.jobrun.spi.Status;
import com.gruelbox.orko.jobrun.spi.StatusUpdateService;

import io.dropwizard.testing.junit.DAOTestRule;

public class TestJobExecutionIntegration {

  private static final Logger LOGGER = LoggerFactory.getLogger(TestJobExecutionIntegration.class);

  @Rule
  public DAOTestRule database = DbTesting.rule()
    .addEntityClass(JobRecord.class)
    .build();

  private static final int WAIT_SECONDS = 10;

  private static final String JOB1 = "JOB1";
  private static final String JOB2 = "JOB2";
  private static final String JOB3 = "JOB3";

  @Mock private Injector injector;
  @Mock private StatusUpdateService statusUpdateService;

  private JobAccess jobAccess;
  private JobLockerImpl jobLocker;
  private EventBus eventBus;
  private JobRunner jobRunner1;
  private JobRunner jobRunner2;
  private GuardianLoop guardianLoop1;
  private GuardianLoop guardianLoop2;
  private Transactionally transactionally;

  private ExecutorService executorService;

  @Before
  public void setup() throws Exception {

    MockitoAnnotations.initMocks(this);

    JobRunConfiguration config = new JobRunConfiguration();
    config.setGuardianLoopSeconds(1);
    config.setDatabaseLockSeconds(4);

    jobLocker = new JobLockerImpl(config,
        DbTesting.connectionSource(database.getSessionFactory()),
        new Transactionally(database.getSessionFactory()));

    DbTesting.clearDatabase();
    DbTesting.invalidateSchemaCache();
    DbTesting.mutateToSupportSchema(schema(
      schema(new JobRecordContribution().tables()),
      schema(new JobLockContribution().tables())
    ));

    eventBus = new EventBus();

    when(injector.getInstance(TestingJobProcessor.Factory.class)).thenReturn(new TestingJobProcessor.Factory() {
      @Override
      public JobProcessor<TestingJob> create(TestingJob job, JobControl jobControl) {
        return new TestingJobProcessor(job, jobControl, eventBus);
      }
    });

    when(injector.getInstance(AsynchronouslySelfStoppingJobProcessor.Factory.class)).thenReturn(new AsynchronouslySelfStoppingJobProcessor.Factory() {
      @Override
      public JobProcessor<AsynchronouslySelfStoppingJob> create(AsynchronouslySelfStoppingJob job, JobControl jobControl) {
        return new AsynchronouslySelfStoppingJobProcessor(job, jobControl, eventBus);
      }
    });

    when(injector.getInstance(CounterJobProcessor.Factory.class)).thenReturn(new CounterJobProcessor.Factory() {
      @Override
      public JobProcessor<CounterJob> create(CounterJob job, JobControl jobControl) {
        return new CounterJobProcessor(job, jobControl, eventBus);
      }
    });

    executorService = Executors.newFixedThreadPool(4);

    transactionally = new Transactionally(database.getSessionFactory());
    jobAccess = new JobAccessImpl(Providers.of(database.getSessionFactory()), new ObjectMapper(), jobLocker);
    jobRunner1 = new JobRunner(jobAccess, jobLocker, injector, eventBus, statusUpdateService,
        transactionally, executorService, Providers.of(database.getSessionFactory()));
    jobRunner2 = new JobRunner(jobAccess, jobLocker, injector, eventBus, statusUpdateService,
        transactionally, executorService, Providers.of(database.getSessionFactory()));
    guardianLoop1 = new GuardianLoop(jobAccess, jobRunner1, eventBus, config, transactionally);
    guardianLoop2 = new GuardianLoop(jobAccess, jobRunner2, eventBus, config, transactionally);
  }


  /**
   * Just does nothing and makes sure we can start and stop cleanly.
   */
  @Test
  public void testNothingRunningCleanShutdown() throws Exception {
    start();
  }


  /**
   * Enqueues three synchronous jobs and makes sure they all complete correctly
   */
  @Test
  public void testSyncRun() throws Exception {
    LOGGER.info("Starting listeners");
    try (Listener listener1 = new Listener(JOB1);
         Listener listener2 = new Listener(JOB2);
         Listener listener3 = new Listener(JOB3)) {

      LOGGER.info("Submitting jobs");
      addJob(TestingJob.builder().id(JOB1).build());
      addJob(TestingJob.builder().id(JOB2).build());
      addJob(TestingJob.builder().id(JOB3).build());

      LOGGER.info("Starting guardians");
      start();

      LOGGER.info("Waiting for success");
      Assert.assertTrue(listener1.awaitFinish());
      Assert.assertTrue(listener2.awaitFinish());
      Assert.assertTrue(listener3.awaitFinish());
    }
  }


  /**
   * Ensures that an exception thrown during startup is treated as transient
   * @throws Exception
   */
  @Test
  public void testFailOnStart() throws Exception {
    try (Listener listener1 = new Listener(JOB1)) {
      addJob(TestingJob.builder().id(JOB1).failOnStart(true).build());
      start();
      Assert.assertTrue(listener1.awaitFinish());
      verify(statusUpdateService).status(JOB1, Status.FAILURE_TRANSIENT);
      verifyNoMoreInteractions(statusUpdateService);
    }
  }


  /**
   * Ensures that an exception thrown during stop is handled
   * @throws Exception
   */
  @Test
  public void testFailOnStopNonResident() throws Exception {
    try (Listener listener1 = new Listener(JOB1)) {
      addJob(TestingJob.builder().id(JOB1).failOnStop(true).build());
      start();
      Assert.assertTrue(listener1.awaitFinish());
      verify(statusUpdateService).status(JOB1, Status.SUCCESS);
      verifyNoMoreInteractions(statusUpdateService);
    }
  }


  /**
   * Ensures that an exception thrown during stop is handled
   * @throws Exception
   */
  @Test
  public void testFailOnStopResident() throws Exception {
    try (Listener listener1 = new Listener(JOB1)) {
      addJob(TestingJob.builder().id(JOB1).runAsync(true).stayResident(false).failOnStop(true).build());
      start();
      Assert.assertTrue(listener1.awaitFinish());

      InOrder inOrder = inOrder(statusUpdateService);
      inOrder.verify(statusUpdateService).status(JOB1, Status.RUNNING);
      inOrder.verify(statusUpdateService).status(JOB1, Status.SUCCESS);
      verifyNoMoreInteractions(statusUpdateService);
    }
  }


  /**
   * Ensures that a job shutdown is handled correctly if it tries to finish
   * asynchronously during the setup phase
   *
   * @throws Exception
   */
  @Test
  public void testCompleteDuringSetup() throws Exception {
    try (Listener listener1 = new Listener(JOB1)) {
      addJob(AsynchronouslySelfStoppingJob.builder().id(JOB1).build());
      start();
      Assert.assertTrue(listener1.awaitFinish());

      InOrder inOrder = inOrder(statusUpdateService);
      inOrder.verify(statusUpdateService).status(JOB1, Status.RUNNING);
      inOrder.verify(statusUpdateService).status(JOB1, Status.SUCCESS);
      verifyNoMoreInteractions(statusUpdateService);
    }
  }


  /**
   * Check for race conditions by using persistent state for a counter.
   *
   * @throws Exception
   */
  @Test
  public void testCounter() throws Exception {
    try (Listener listener1 = new Listener(JOB1)) {
      addJob(CounterJob.builder().id(JOB1).build());
      start();
      Assert.assertTrue(listener1.awaitFinish());

      InOrder inOrder = inOrder(statusUpdateService);
      inOrder.verify(statusUpdateService).status(JOB1, Status.RUNNING);
      inOrder.verify(statusUpdateService).status(JOB1, Status.SUCCESS);
      verifyNoMoreInteractions(statusUpdateService);

      List<Integer> actual = listener1.counters;
      List<Integer> expected = IntStream.range(2, 101).boxed().collect(Collectors.toList());

      LOGGER.info("actual={}", actual);
      LOGGER.info("expected={}", expected);
      assertEquals(expected, actual);
    }
  }


  /**
   * Ensures that we correctly handle a mid-run abort
   * @throws Exception
   */
  @Test
  public void testFailOnTick() throws Exception {
    try (Listener listener1 = new Listener(JOB1)) {
      addJob(TestingJob.builder().id(JOB1).runAsync(true).stayResident(true).failOnTick(true).build());
      start();
      Assert.assertTrue(listener1.awaitFinish());

      InOrder inOrder = inOrder(statusUpdateService);
      inOrder.verify(statusUpdateService).status(JOB1, Status.RUNNING);
      inOrder.verify(statusUpdateService).status(JOB1, Status.FAILURE_PERMANENT);
      verifyNoMoreInteractions(statusUpdateService);
    }
  }


  /**
   * This job should start and then stay resident until forcefully killed.
   */
  @Test
  public void testASyncResidentRunKillByLostLock() throws Exception {
    try (Listener listener1 = new Listener(JOB1)) {
      addJob(TestingJob.builder().id(JOB1).runAsync(true).stayResident(true).build());

      start();

      // Make sure we're up and running
      Assert.assertTrue(listener1.awaitStart());

      // Should still be running after a pause
      Assert.assertFalse(listener1.awaitFinish());

      // Kill the guardians so the lock stops getting refreshed
      guardianLoop1.kill();
      guardianLoop2.kill();

      // Delete the lock and tell the job to refresh
      database.inTransaction(() -> jobLocker.releaseAllLocks());
      executorService.execute(() -> eventBus.post(KeepAliveEvent.INSTANCE));

      // Should die
      Assert.assertTrue(listener1.awaitFinish());
    }
  }


  /**
   * This job should start and then stay resident until we kill them by shutting down the system.
   */
  @Test
  public void testASyncResidentRunKillByShutdown() throws Exception {
    try (Listener listener1 = new Listener(JOB1)) {

      addJob(TestingJob.builder().id(JOB1).runAsync(true).stayResident(true).build());

      start();

      // Make sure we're up and running
      Assert.assertTrue(listener1.awaitStart());

      // Should still be running after a pause
      Assert.assertFalse(listener1.awaitFinish());

      // Shut down
      stopGuardians();

      // Should die
      Assert.assertTrue(listener1.awaitFinish());
    }
  }


  /**
   * Handle jobs getting updated and continuing.
   */
  @Test
  public void testUpdate() throws Exception {
    try (Listener listener1 = new Listener(JOB1)) {

      addJob(TestingJob.builder().id(JOB1).runAsync(true).stayResident(true).update(true).build());

      start();

      Assert.assertTrue(listener1.awaitStart());
      Assert.assertFalse(listener1.awaitFinish());

      // Shut down
      stopGuardians();

      // Should die
      Assert.assertTrue(listener1.awaitFinish());
    }
  }


  /**
   * Three jobs which should run a single tick then stop.
   */
  @Test
  public void testASyncNonResidentRun() throws Exception {
    try (Listener listener1 = new Listener(JOB1);
        Listener listener2 = new Listener(JOB2);
        Listener listener3 = new Listener(JOB3)) {
      addJob(TestingJob.builder().id(JOB1).runAsync(true).stayResident(false).build());
      addJob(TestingJob.builder().id(JOB2).runAsync(true).stayResident(false).build());
      addJob(TestingJob.builder().id(JOB3).runAsync(true).stayResident(false).build());

      start();

      Assert.assertTrue(listener1.awaitFinish());
      Assert.assertTrue(listener1.awaitFinish());
      Assert.assertTrue(listener1.awaitFinish());
    }
  }


  /**
   * Makes sure a job doesn't start if the keepalive thinks it's already running.
   */
  @Test
  public void testDontRunIfHandledElsewhere() throws Exception {
    try (Listener listener1 = new Listener(JOB1)) {
      addJob(TestingJob.builder().id(JOB1).build());

      UUID myId = UUID.randomUUID();
      database.inTransaction(() -> assertTrue(jobLocker.attemptLock(JOB1, myId)));
      Future<?> backgroundLock = executorService.submit(() -> {
        while (!Thread.currentThread().isInterrupted()) {
          try {
            Thread.sleep(500);
          } catch (InterruptedException e) {
            break;
          }
          transactionally.run(() -> jobLocker.updateLock(JOB1, myId));
        }
      });

      start();

      Assert.assertFalse(listener1.awaitStart());

      backgroundLock.cancel(true);
    }
  }


  private UUID addJob(Job job) {
    return database.inTransaction(() -> {
      jobAccess.insert(job);
      return null;
    });
  }

  private void start() throws Exception {
    jobLocker.start();
    guardianLoop1.startAsync();
    guardianLoop2.startAsync();
    guardianLoop1.awaitRunning();
    guardianLoop2.awaitRunning();
  }

  @After
  public void tearDown() throws Exception {
    jobLocker.stop();
    stopGuardians();
  }

  private void stopGuardians() {
    if (guardianLoop1 != null)
      guardianLoop1.stopAsync();
    if (guardianLoop2 != null)
      guardianLoop2.stopAsync();
    if (guardianLoop1 != null)
      guardianLoop1.awaitTerminated();
    if (guardianLoop2 != null)
      guardianLoop2.awaitTerminated();
  }

  private final class Listener implements AutoCloseable {

    private final CountDownLatch started;
    private final CountDownLatch completed;
    private final String jobId;
    private final List<Integer> counters = Lists.newCopyOnWriteArrayList();

    Listener(String jobId) {
      this.jobId = jobId;
      started = new CountDownLatch(1);
      completed = new CountDownLatch(1);
      eventBus.register(this);
    }

    @Subscribe
    void onEvent(TestingJobEvent event) {
      if (event.eventType() == EventType.FINISH && event.jobId().equals(jobId)) {
        completed.countDown();
      }
      if (event.eventType() == EventType.START && event.jobId().equals(jobId)) {
        started.countDown();
      }
    }

    @Subscribe
    void onCount(Integer event) {
      counters.add(event);
    }

    boolean awaitFinish() throws InterruptedException {
      return completed.await(WAIT_SECONDS, TimeUnit.SECONDS);
    }

    boolean awaitStart() throws InterruptedException {
      return started.await(WAIT_SECONDS, TimeUnit.SECONDS);
    }

    @Override
    public void close() {
      eventBus.unregister(this);
    }
  }
}
