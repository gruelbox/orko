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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.common.util.concurrent.RateLimiter;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.gruelbox.orko.db.Transactionally;
import com.gruelbox.orko.exception.OrkoAbortException;
import com.gruelbox.orko.jobrun.spi.Job;
import com.gruelbox.orko.jobrun.spi.JobRunConfiguration;
import java.util.concurrent.CountDownLatch;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Background process which restarts any open jobs which aren't currently running on any instance.
 *
 * @author Graham Crockford
 */
@Singleton
class GuardianLoop extends AbstractExecutionThreadService {

  private static final Logger LOGGER = LoggerFactory.getLogger(GuardianLoop.class);

  private final JobAccess jobAccess;
  private final JobRunner jobRunner;
  private final EventBus eventBus;
  private final Transactionally transactionally;
  private final Provider<SessionFactory> sessionFactory;
  private final RateLimiter rateLimiter;

  private volatile boolean kill;
  private final CountDownLatch killed = new CountDownLatch(1);

  @Inject
  GuardianLoop(
      JobAccess jobaccess,
      JobRunner jobRunner,
      EventBus eventBus,
      JobRunConfiguration config,
      Transactionally transactionally,
      Provider<SessionFactory> sessionFactory) {
    jobAccess = jobaccess;
    this.jobRunner = jobRunner;
    this.eventBus = eventBus;
    this.transactionally = transactionally;
    this.sessionFactory = sessionFactory;
    this.rateLimiter = RateLimiter.create(2.0D / config.getGuardianLoopSeconds());
  }

  @Override
  public void run() {
    Thread.currentThread().setName("Guardian loop");
    LOGGER.info("{} started", this);
    while (isRunning() && !kill) {
      try {
        if (Thread.currentThread().isInterrupted()) {
          throw new OrkoAbortException("thread interrupted");
        }

        rateLimiter.acquire();

        LOGGER.debug("{} checking and restarting jobs", this);
        checkSessionFactoryState();
        lockAndStartInactiveJobs();

        rateLimiter.acquire();

        LOGGER.debug("{} refreshing locks", this);
        checkSessionFactoryState();
        eventBus.post(KeepAliveEvent.INSTANCE);

      } catch (OrkoAbortException e) {
        LOGGER.info("{} shutting down: {}", this, e.getMessage());
        break;
      } catch (Exception e) {
        LOGGER.error("Error in keep-alive loop", e);
      }
    }
    if (kill) {
      killed.countDown();
      LOGGER.warn("{} killed (should only ever happen in test code)", this);
    } else {
      eventBus.post(StopEvent.INSTANCE);
      LOGGER.info("{} stopped", this);
    }
  }

  /**
   * When the app is forcibly killed, this seems to happen a lot and sends the app into an endless
   * loop, so for the time being this will break the cycle.
   *
   * @throws OrkoAbortException if the session factory is closed.
   */
  private void checkSessionFactoryState() throws OrkoAbortException {
    if (sessionFactory.get().isClosed()) {
      throw new OrkoAbortException("session factory closed");
    }
  }

  private void lockAndStartInactiveJobs() {
    boolean foundJobs = false;
    for (Job job : transactionally.call(jobAccess::list)) {
      foundJobs = true;
      try {
        transactionally.callChecked(
            () -> {
              jobRunner.submitExisting(job);
              return null;
            });
      } catch (Exception e) {
        LOGGER.error("Failed to start job [" + job + "]", e);
      }
    }
    if (!foundJobs) {
      LOGGER.debug("Nothing running");
    }
  }

  @Override
  protected String serviceName() {
    return getClass().getSimpleName() + "[" + System.identityHashCode(this) + "]";
  }

  @VisibleForTesting
  void kill() throws InterruptedException {
    kill = true;
    killed.await();
  }
}
