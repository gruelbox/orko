package com.gruelbox.orko.jobrun;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
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

  private final JobAccess advancedOrderAccess;
  private final JobRunner jobSubmitter;
  private final EventBus eventBus;
  private final JobRunConfiguration config;

  @Inject
  GuardianLoop(JobAccess jobaccess,
               JobRunner jobRunner,
               EventBus eventBus,
               JobRunConfiguration config) {
    advancedOrderAccess = jobaccess;
    jobSubmitter = jobRunner;
    this.eventBus = eventBus;
    this.config = config;
  }

  @Override
  public void run() {
    Thread.currentThread().setName("Guardian loop");
    LOGGER.info(this + " started");
    while (isRunning() && !Thread.currentThread().isInterrupted()) {
      try {

        lockAndStartInactiveJobs();

        try {
          Thread.sleep((long) config.getGuardianLoopSeconds() * 1000);
        } catch (InterruptedException e) {
          LOGGER.info("Shutting down " + this);
          Thread.currentThread().interrupt();
          break;
        }

        // Refresh the locks on the running jobs
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