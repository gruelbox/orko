package com.grahamcrockford.oco.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.grahamcrockford.oco.job.Alert;
import com.grahamcrockford.oco.job.Alert.AlertLevel;
import com.grahamcrockford.oco.submit.JobSubmitter;

class RetryingNotificationService implements NotificationService {

  private static final Logger LOGGER = LoggerFactory.getLogger(RetryingNotificationService.class);

  private final JobSubmitter jobSubmitter;

  @Inject
  RetryingNotificationService(JobSubmitter jobSubmitter) {
    this.jobSubmitter = jobSubmitter;
  }

  @Override
  public void info(String message) {
    jobSubmitter.submitNewUnchecked(Alert.builder().message(message).level(AlertLevel.INFO).build());
  }

  @Override
  public void error(String message) {
    jobSubmitter.submitNewUnchecked(Alert.builder().message(message).level(AlertLevel.ERROR).build());
  }

  @Override
  public void error(String message, Throwable cause) {
    LOGGER.error("Error notification: " + message, cause);
    jobSubmitter.submitNewUnchecked(Alert.builder().message(message).level(AlertLevel.ERROR).build());
  }
}