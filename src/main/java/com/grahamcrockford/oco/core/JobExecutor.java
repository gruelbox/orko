package com.grahamcrockford.oco.core;

import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.grahamcrockford.oco.api.AdvancedOrder;
import com.grahamcrockford.oco.api.AdvancedOrderProcessor;
import com.grahamcrockford.oco.db.AdvancedOrderAccess;
import com.grahamcrockford.oco.db.JobLocker;

public class JobExecutor implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(JobExecutor.class);

  private AdvancedOrder job;
  private final UUID uuid;
  private final AdvancedOrderProcessor<AdvancedOrder> processor;
  private final TelegramService telegramService;
  private final AdvancedOrderAccess advancedOrderAccess;
  private final JobLocker jobLocker;

  @SuppressWarnings("unchecked")
  @AssistedInject
  JobExecutor(@Assisted AdvancedOrder job, @Assisted UUID uuid, Injector injector,
      TelegramService telegramService, AdvancedOrderAccess advancedOrderAccess,
      JobLocker jobLocker) {
    this.job = job;
    this.telegramService = telegramService;
    this.advancedOrderAccess = advancedOrderAccess;
    this.jobLocker = jobLocker;
    this.uuid = uuid;
    this.processor = (AdvancedOrderProcessor<AdvancedOrder>) injector.getInstance(job.processor());
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Override
  public void run() {
    LOGGER.info(this + " started");
    while (true) {
      try {

        Optional<AdvancedOrder> updated = processor.process(job);

        if (!updated.isPresent()) {
          advancedOrderAccess.delete(job.id());
          jobLocker.releaseLock(job.id(), uuid);
          return;
        } else {
          if (!updated.get().equals(job)) {
            job = updated.get();
            LOGGER.debug("Saving updated job: " + job);
            advancedOrderAccess.update(job, (Class) job.getClass());
          }
        }

      } catch (InterruptedException e) {
        jobLocker.releaseLock(job.id(), uuid);
        break;
      } catch (Exception e) {
        LOGGER.error("Failed to handle job #" + job.id(), e);
        telegramService.sendMessage("Error handling job " + job.id() + ": " + e.getMessage());
      }

      if (!jobLocker.updateLock(job.id(), uuid))
        break;
    }
    LOGGER.info(this + " stopped");
  }

  @Override
  public String toString() {
    return "JobExecutor[" + job + "]";
  }


  public interface Factory {
    public JobExecutor create(AdvancedOrder job, UUID uuid);
  }
}