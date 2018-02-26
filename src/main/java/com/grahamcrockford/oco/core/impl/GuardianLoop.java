package com.grahamcrockford.oco.core.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.grahamcrockford.oco.core.api.JobSubmitter;
import com.grahamcrockford.oco.core.spi.Job;
import com.grahamcrockford.oco.core.spi.KeepAliveEvent;

@Singleton
class GuardianLoop extends AbstractExecutionThreadService {

  private static final Logger LOGGER = LoggerFactory.getLogger(GuardianLoop.class);

  private final JobAccess advancedOrderAccess;
  private final JobSubmitter jobSubmitter;
  private final AsyncEventBus asyncEventBus;

  @Inject
  GuardianLoop(JobAccess advancedOrderAccess,
               JobSubmitter jobSubmitter,
               AsyncEventBus asyncEventBus) {
    this.advancedOrderAccess = advancedOrderAccess;
    this.jobSubmitter = jobSubmitter;
    this.asyncEventBus = asyncEventBus;
  }

  @Override
  public void run() {
    Thread.currentThread().setName("Guardian loop");
    LOGGER.info(this + " started");
    while (isRunning()) {
      try {

        lockAndStartInactiveJobs();

        try {
          Thread.sleep(5000);
        } catch (InterruptedException e) {
          LOGGER.info("Shutting down " + this);
          break;
        }

        asyncEventBus.post(KeepAliveEvent.INSTANCE);

      } catch (Throwable e) {
        LOGGER.error("Error in keep-alive loop", e);
      }
    }
    asyncEventBus.post(StopEvent.INSTANCE);
    LOGGER.info(this + " stopped");
  }

  private void lockAndStartInactiveJobs() {
    boolean foundJobs = false;
    boolean locksFailed = false;
    for (Job job : advancedOrderAccess.list()) {
      foundJobs = true;
      locksFailed = !jobSubmitter.submitExisting(job);
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
}