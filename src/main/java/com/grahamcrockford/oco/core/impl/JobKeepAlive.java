package com.grahamcrockford.oco.core.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.grahamcrockford.oco.core.api.JobSubmitter;
import com.grahamcrockford.oco.core.spi.Job;

class JobKeepAlive extends AbstractExecutionThreadService {

  private static final Logger LOGGER = LoggerFactory.getLogger(JobKeepAlive.class);

  private final JobAccess advancedOrderAccess;
  private final JobSubmitter jobSubmitter;

  @Inject
  JobKeepAlive(JobAccess advancedOrderAccess,
               JobSubmitter jobSubmitter) {
    this.advancedOrderAccess = advancedOrderAccess;
    this.jobSubmitter = jobSubmitter;
  }

  @Override
  public void run() {
    LOGGER.info(this + " started");
    while (isRunning()) {
      try {
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

  @Override
  protected void shutDown() throws Exception {
    jobSubmitter.shutdown();
    super.shutDown();
  }

  @Override
  protected String serviceName() {
    return getClass().getSimpleName() + "[" + System.identityHashCode(this) + "]";
  }

  static final class ProviderA implements Provider<JobKeepAlive> {

    private final JobKeepAlive jobKeepAlive;

    @Inject
    ProviderA(JobKeepAlive jobKeepAlive) {
      this.jobKeepAlive = jobKeepAlive;
    }

    @Override
    public JobKeepAlive get() {
      return jobKeepAlive;
    }
  }

  static final class ProviderB implements Provider<JobKeepAlive> {

    private final JobKeepAlive jobKeepAlive;

    @Inject
    ProviderB(JobKeepAlive jobKeepAlive) {
      this.jobKeepAlive = jobKeepAlive;
    }

    @Override
    public JobKeepAlive get() {
      return jobKeepAlive;
    }
  }
}