package com.gruelbox.orko.jobrun;

/*-
 * ===============================================================================L
 * Orko Job
 * ================================================================================
 * Copyright (C) 2018 - 2019 Graham Crockford
 * ================================================================================
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * ===============================================================================E
 */

import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.gruelbox.orko.db.Transactionally;
import com.gruelbox.orko.jobrun.spi.Job;
import com.gruelbox.orko.jobrun.spi.JobRunConfiguration;


/**
 * Background process which restarts any open jobs which aren't
 * currently running on any instance.
 *
 * @author Graham Crockford
 */
@Singleton
class GuardianLoop extends AbstractExecutionThreadService {

  private static final Logger LOGGER = LoggerFactory.getLogger(GuardianLoop.class);

  private final JobAccess jobAccess;
  private final JobRunner jobRunner;
  private final EventBus eventBus;
  private final JobRunConfiguration config;
  private final Transactionally transactionally;
  private volatile boolean kill;
  private final CountDownLatch killed = new CountDownLatch(1);

  @Inject
  GuardianLoop(JobAccess jobaccess,
               JobRunner jobRunner,
               EventBus eventBus,
               JobRunConfiguration config,
               Transactionally transactionally) {
    jobAccess = jobaccess;
    this.jobRunner = jobRunner;
    this.eventBus = eventBus;
    this.config = config;
    this.transactionally = transactionally;
  }

  @Override
  public void run() {
    Thread.currentThread().setName("Guardian loop");
    LOGGER.info(this + " started");
    while (isRunning() && !Thread.currentThread().isInterrupted() && !kill) {
      try {

        LOGGER.debug("Checking and restarting jobs");
        lockAndStartInactiveJobs();

        LOGGER.debug("Sleeping");
        try {
          Thread.sleep((long) config.getGuardianLoopSeconds() * 1000);
        } catch (InterruptedException e) {
          LOGGER.info("Shutting down " + this);
          Thread.currentThread().interrupt();
          break;
        }

        // Refresh the locks on the running jobs
        LOGGER.debug("Refreshing locks");
        eventBus.post(KeepAliveEvent.INSTANCE);

      } catch (Exception e) {
        LOGGER.error("Error in keep-alive loop", e);
      }
    }
    if (kill) {
      killed.countDown();
      LOGGER.warn(this + " killed (should only ever happen in test code)");
    } else {
      eventBus.post(StopEvent.INSTANCE);
      LOGGER.info(this + " stopped");
    }
  }

  private void lockAndStartInactiveJobs() {
    boolean foundJobs = false;
    boolean locksFailed = false;

    Iterable<Job> jobs = transactionally.call(() -> jobAccess.list());
    for (Job job : jobs) {
      foundJobs = true;
      try {
        transactionally.callChecked(() -> {
          jobRunner.submitExisting(job);
          return null;
        });
      } catch (Exception e) {
        LOGGER.error("Failed to start job [" + job + "]", e);
      }
    }
    if (!foundJobs) {
      LOGGER.debug("Nothing running");
    } else if (locksFailed) {
      LOGGER.debug("Nothing new to run");
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
