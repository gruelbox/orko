package com.gruelbox.orko.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.gruelbox.orko.db.Transactionally;
import com.gruelbox.orko.job.Alert;
import com.gruelbox.orko.job.StatusUpdateJob;
import com.gruelbox.orko.jobrun.JobSubmitter;
import com.gruelbox.orko.jobrun.spi.StatusUpdate;
import com.gruelbox.orko.jobrun.spi.StatusUpdateService;

@Singleton
class RetryingMessageService implements NotificationService, StatusUpdateService {

  private static final Logger LOGGER = LoggerFactory.getLogger(RetryingMessageService.class);

  private final JobSubmitter jobSubmitter;
  private final Transactionally transactionally;

  @Inject
  RetryingMessageService(JobSubmitter jobSubmitter, Transactionally transactionally) {
    this.jobSubmitter = jobSubmitter;
    this.transactionally = transactionally;
  }


  @Override
  public void send(Notification notification) {
    transactionally.allowingNested().run(() ->
      jobSubmitter.submitNewUnchecked(Alert.builder().notification(notification).build())
    );
  }

  @Override
  public void error(String message, Throwable cause) {
    LOGGER.error("Error notification: " + message, cause);
    error(message);
  }


  @Override
  public void send(StatusUpdate statusUpdate) {
    transactionally.allowingNested().run(() ->
      jobSubmitter.submitNewUnchecked(StatusUpdateJob.builder().statusUpdate(statusUpdate).build())
    );
  }
}