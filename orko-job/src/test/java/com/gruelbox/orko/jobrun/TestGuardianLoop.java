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

import static com.gruelbox.orko.db.MockTransactionallyFactory.mockTransactionally;
import static java.util.Collections.emptyList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import com.google.common.eventbus.EventBus;
import com.google.inject.util.Providers;
import com.gruelbox.orko.db.Transactionally;
import com.gruelbox.orko.jobrun.spi.JobRunConfiguration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

public class TestGuardianLoop {

  private static final int WAIT = 20;
  @Mock private JobAccess jobAccess;
  @Mock private JobRunner jobRunner;
  @Mock private EventBus eventBus;
  @Mock private JobRunConfiguration config;
  private final Transactionally transactionally = mockTransactionally();
  @Mock private SessionFactory sessionFactory;

  private GuardianLoop guardianLoop;
  private final CountDownLatch oneLoopCompleted = new CountDownLatch(1);
  private final AtomicReference<Thread> thread = new AtomicReference<>();

  @Before
  public void setup() throws TimeoutException {
    initMocks(this);
    when(jobAccess.list()).thenReturn(emptyList());
    doAnswer(
            i -> {
              thread.set(Thread.currentThread());
              oneLoopCompleted.countDown();
              return null;
            })
        .when(eventBus)
        .post(KeepAliveEvent.INSTANCE);
    when(config.getGuardianLoopSeconds()).thenReturn(1);
    guardianLoop =
        new GuardianLoop(
            jobAccess, jobRunner, eventBus, config, transactionally, Providers.of(sessionFactory));
  }

  @Test
  public void testImmediateStartupShutdown() throws TimeoutException {
    guardianLoop.startAsync().awaitRunning(WAIT, SECONDS);
    guardianLoop.stopAsync().awaitTerminated(WAIT, SECONDS);
  }

  @Test
  public void testControlledStartupShutdown() throws TimeoutException, InterruptedException {
    guardianLoop.startAsync().awaitRunning(WAIT, SECONDS);
    try {
      assertTrue(oneLoopCompleted.await(WAIT, SECONDS));
    } finally {
      guardianLoop.stopAsync().awaitTerminated(WAIT, SECONDS);
    }
  }

  @Test
  public void testInterrupt() throws TimeoutException, InterruptedException {
    guardianLoop.startAsync().awaitRunning(WAIT, SECONDS);
    try {
      assertTrue(oneLoopCompleted.await(WAIT, SECONDS));
      thread.get().interrupt();
      thread.get().join(WAIT * 1000, 0);
      assertFalse(guardianLoop.isRunning());
    } finally {
      guardianLoop.stopAsync().awaitTerminated(WAIT, SECONDS);
    }
  }

  @Test
  public void testCloseSessionFactory() throws TimeoutException, InterruptedException {
    guardianLoop.startAsync().awaitRunning(WAIT, SECONDS);
    try {
      assertTrue(oneLoopCompleted.await(WAIT, SECONDS));
      when(sessionFactory.isClosed()).thenReturn(true);
      thread.get().join(WAIT * 1000, 0);
      assertFalse(guardianLoop.isRunning());
    } finally {
      guardianLoop.stopAsync().awaitTerminated(WAIT, SECONDS);
    }
  }

  @Test
  public void testStaysAliveOnOtherExceptions() throws TimeoutException, InterruptedException {
    guardianLoop.startAsync().awaitRunning(WAIT, SECONDS);
    try {
      assertTrue(oneLoopCompleted.await(WAIT, SECONDS));
      when(jobAccess.list()).thenThrow(RuntimeException.class);

      // Halting problem! Give it three seconds and make sure it's still up.
      Thread.sleep(3000);
      assertTrue(guardianLoop.isRunning());

      // Make sure we didn't loop madly
      Mockito.verify(jobAccess, atMost(10)).list();

    } finally {
      guardianLoop.stopAsync().awaitTerminated(WAIT, SECONDS);
    }
  }
}
