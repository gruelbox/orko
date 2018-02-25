package com.grahamcrockford.oco.core.impl;

import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.grahamcrockford.oco.core.spi.Job;
import com.grahamcrockford.oco.core.spi.JobProcessor;
import com.grahamcrockford.oco.telegram.TelegramService;

class JobExecutor implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(JobExecutor.class);

  private Job job;
  private final UUID uuid;
  private final JobProcessor<Job> processor;
  private final TelegramService telegramService;
  private final JobAccess advancedOrderAccess;
  private final JobLocker jobLocker;

  @SuppressWarnings("unchecked")
  @AssistedInject
  JobExecutor(@Assisted Job job, @Assisted UUID uuid, Injector injector,
      TelegramService telegramService, JobAccess advancedOrderAccess,
      JobLocker jobLocker) {
    this.job = job;
    this.telegramService = telegramService;
    this.advancedOrderAccess = advancedOrderAccess;
    this.jobLocker = jobLocker;
    this.uuid = uuid;
    this.processor = (JobProcessor<Job>) injector.getInstance(job.processor());
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Override
  public void run() {
    LOGGER.info(this + " started");
    while (!Thread.currentThread().isInterrupted()) {
      try {

        Optional<Job> updated = processor.process(job);

        if (!updated.isPresent()) {
          advancedOrderAccess.delete(job.id());
          break;
        }

        if (!updated.get().equals(job)) {
          job = updated.get();
          LOGGER.debug("Saving updated job: " + job);
          advancedOrderAccess.update(job, (Class) job.getClass());
        }

        if (!jobLocker.updateLock(job.id(), uuid))
          break;

      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        break;
      } catch (Exception e) {
        LOGGER.error("Failed to handle job #" + job.id(), e);
        telegramService.sendMessage("Error handling job " + job.id() + ": " + e.getMessage());
        LOGGER.info(this + " failed. Not releasing lock. Retry expected after a short delay");
        throw e;
      }
    }
    jobLocker.releaseLock(job.id(), uuid);
    LOGGER.info(this + " stopped");
  }

  @Override
  public String toString() {
    return "JobExecutor[" + job + "]";
  }


  public interface Factory {
    public JobExecutor create(Job job, UUID uuid);
  }
}