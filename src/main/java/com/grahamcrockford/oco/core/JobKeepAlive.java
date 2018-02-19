package com.grahamcrockford.oco.core;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.grahamcrockford.oco.api.Job;
import com.grahamcrockford.oco.core.JobExecutor.Factory;
import com.grahamcrockford.oco.db.JobAccess;
import com.grahamcrockford.oco.db.JobLocker;

@Singleton
class JobKeepAlive extends AbstractExecutionThreadService {

  private static final Logger LOGGER = LoggerFactory.getLogger(JobKeepAlive.class);

  private final JobAccess advancedOrderAccess;
  private final JobLocker jobLocker;
  private final Factory jobExecutorFactory;

  @Inject
  JobKeepAlive(JobAccess advancedOrderAccess,
               JobLocker jobLocker,
               JobExecutor.Factory jobExecutorFactory) {
    this.advancedOrderAccess = advancedOrderAccess;
    this.jobLocker = jobLocker;
    this.jobExecutorFactory = jobExecutorFactory;
  }

  @Override
  public void run() {
    LOGGER.info(this + " started");
    while (true) {
      try {
        boolean foundJobs = false;
        boolean locksFailed = false;
        for (Job job : advancedOrderAccess.list()) {
          foundJobs = true;
          UUID uuid = UUID.randomUUID();
          if (jobLocker.attemptLock(job.id(), uuid)) {
            job = advancedOrderAccess.load(job.id());
            new Thread(jobExecutorFactory.create(job, uuid)).start();
          } else {
            locksFailed = true;
          }
        }
        if (!foundJobs) {
          LOGGER.debug("Nothing running");
        } else if (locksFailed) {
          LOGGER.debug("Nothing new to run");
        }
      } catch (Exception e) {
        LOGGER.error("Error in keep-alive loop", e);
      }
      try {
        Thread.sleep(5000);
      } catch (InterruptedException e) {
        LOGGER.info("Shutting down " + this);
        break;
      }
    }
    LOGGER.info(this + " stopped");
  }
}