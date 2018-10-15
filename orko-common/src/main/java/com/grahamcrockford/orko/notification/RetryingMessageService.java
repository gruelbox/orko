package com.grahamcrockford.orko.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.grahamcrockford.orko.job.Alert;
import com.grahamcrockford.orko.job.StatusUpdateJob;
import com.grahamcrockford.orko.submit.JobSubmitter;

@Singleton
class RetryingMessageService implements NotificationService, StatusUpdateService {

  private static final Logger LOGGER = LoggerFactory.getLogger(RetryingMessageService.class);

  private final JobSubmitter jobSubmitter;

  @Inject
  RetryingMessageService(JobSubmitter jobSubmitter) {
    this.jobSubmitter = jobSubmitter;
  }


  @Override
  public void send(Notification notification) {
    jobSubmitter.submitNewUnchecked(Alert.builder().notification(notification).build());
  }

  @Override
  public void error(String message, Throwable cause) {
    LOGGER.error("Error notification: " + message, cause);
    error(message);
  }


  @Override
  public void send(StatusUpdate statusUpdate) {
    jobSubmitter.submitNewUnchecked(StatusUpdateJob.builder().statusUpdate(statusUpdate).build());
  }
}