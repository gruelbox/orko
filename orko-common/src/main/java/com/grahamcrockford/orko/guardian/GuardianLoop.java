package com.grahamcrockford.orko.guardian;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.grahamcrockford.orko.OrkoConfiguration;
import com.grahamcrockford.orko.spi.Job;
import com.grahamcrockford.orko.submit.JobAccess;

@Singleton
class GuardianLoop extends AbstractExecutionThreadService {

  private static final Logger LOGGER = LoggerFactory.getLogger(GuardianLoop.class);

  private final JobAccess advancedOrderAccess;
  private final JobRunner jobSubmitter;
  private final EventBus eventBus;
  private final OrkoConfiguration orkoConfiguration;

  @Inject
  GuardianLoop(JobAccess jobaccess,
               JobRunner jobRunner,
               EventBus eventBus,
               OrkoConfiguration orkoConfiguration) {
    this.advancedOrderAccess = jobaccess;
    this.jobSubmitter = jobRunner;
    this.eventBus = eventBus;
    this.orkoConfiguration = orkoConfiguration;
  }

  @Override
  public void run() {
    Thread.currentThread().setName("Guardian loop");
    LOGGER.info(this + " started");
    while (isRunning() && !Thread.currentThread().isInterrupted()) {
      try {

        lockAndStartInactiveJobs();

        try {
          Thread.sleep((long) orkoConfiguration.getLoopSeconds() * 1000);
        } catch (InterruptedException e) {
          LOGGER.info("Shutting down " + this);
          Thread.currentThread().interrupt();
          break;
        }

        eventBus.post(KeepAliveEvent.INSTANCE);

      } catch (Exception e) {
        LOGGER.error("Error in keep-alive loop", e);
      }
    }
    eventBus.post(StopEvent.INSTANCE);
    LOGGER.info(this + " stopped");
  }

  private void lockAndStartInactiveJobs() {
    boolean foundJobs = false;
    boolean locksFailed = false;
    for (Job job : advancedOrderAccess.list()) {
      foundJobs = true;
      try {
        locksFailed = !jobSubmitter.runExisting(job);
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
}