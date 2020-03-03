/**
 * Orko - Copyright © 2018-2019 Graham Crockford
 *
 * <p>This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * <p>You should have received a copy of the GNU Affero General Public License along with this
 * program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.gruelbox.orko.exchange;

import static java.util.Collections.emptySet;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.gruelbox.orko.wiring.BackgroundProcessingConfiguration;
import java.util.Set;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Background process implementation of {@link MarketDataSubscriptionManager} which decouples the
 * incoming and outgoing streams using publishers to allow streams to be disconnected and
 * reconnected, and allows snapshot-type streams to cache the last snapshot for immediate delivery
 * upon subscription.
 */
abstract class AbstractPollingController extends AbstractExecutionThreadService
    implements SubscriptionController {

  protected final Logger logger = LoggerFactory.getLogger(getClass());

  private final BackgroundProcessingConfiguration configuration;
  protected final SubscriptionPublisher publisher;
  private final Phaser phaser = new Phaser(1);

  private LifecycleListener lifecycleListener = new LifecycleListener() {};

  protected AbstractPollingController(
      BackgroundProcessingConfiguration configuration, SubscriptionPublisher publisher) {
    this.configuration = configuration;
    this.publisher = publisher;
    this.publisher.setController(this);
  }

  /**
   * Updates the subscriptions for the specified exchanges on the next loop tick. The delay is to
   * avoid a large number of new subscriptions in quick succession causing rate bans on exchanges.
   * Call with an empty set to cancel all subscriptions. None of the streams will return anything
   * until this is called, but there is no strict order in which they need to be called.
   *
   * @param subscriptions The subscriptions.
   */
  @Override
  public abstract void updateSubscriptions(Set<MarketDataSubscription> subscriptions);

  @Override
  protected final void run() {
    Thread.currentThread().setName(getClass().getSimpleName());
    logger.info("{} started", this);
    try {
      doRun();
      if (Thread.interrupted()) {
        throw new InterruptedException();
      }
    } catch (InterruptedException e) {
      logger.error("{} stopping due to interrupt", this, e);
      Thread.currentThread().interrupt();
    } catch (Exception e) {
      logger.error("{} stopping due to uncaught exception", this, e);
    } finally {
      updateSubscriptions(emptySet());
      logger.info("{} stopped", this);
      lifecycleListener.onStopMain();
    }
  }

  protected abstract void doRun() throws InterruptedException;

  protected void wake() {
    int phase = phaser.arrive();
    logger.debug("Progressing to phase {}", phase);
  }

  protected boolean isTerminated() {
    return phaser.isTerminated();
  }

  protected int getPhase() {
    return phaser.getPhase();
  }

  protected void subtaskStopped(String subTaskName) {
    lifecycleListener.onStop(subTaskName);
  }

  @Override
  protected void triggerShutdown() {
    super.triggerShutdown();
    phaser.arriveAndDeregister();
    phaser.forceTermination();
  }

  protected void suspend(String subTaskName, int phase, boolean failed)
      throws InterruptedException {
    logger.debug("{} - poll going to sleep", subTaskName);
    try {
      if (failed) {
        long defaultSleep = (long) configuration.getLoopSeconds() * 1000;
        phaser.awaitAdvanceInterruptibly(phase, defaultSleep, TimeUnit.MILLISECONDS);
      } else {
        logger.debug("{} - sleeping until phase {}", subTaskName, phase);
        lifecycleListener.onBlocked(subTaskName);
        phaser.awaitAdvanceInterruptibly(phase);
        logger.debug("{} - poll woken up on request", subTaskName);
      }
    } catch (TimeoutException e) {
      // fine
    } catch (InterruptedException e) {
      throw e;
    } catch (Exception e) {
      logger.error("Failure in phaser wait for " + subTaskName, e);
    }
  }

  @VisibleForTesting
  void setLifecycleListener(LifecycleListener listener) {
    this.lifecycleListener = listener;
  }
}
