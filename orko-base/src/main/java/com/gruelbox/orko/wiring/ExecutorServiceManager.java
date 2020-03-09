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
package com.gruelbox.orko.wiring;

import com.google.inject.Singleton;
import io.dropwizard.lifecycle.Managed;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;

@Singleton
class ExecutorServiceManager implements Managed {

  private final ExecutorService executor;

  ExecutorServiceManager() {
    BasicThreadFactory threadFactory =
        new BasicThreadFactory.Builder()
            .namingPattern("AsyncEventBus-%d")
            .daemon(false)
            .priority(Thread.NORM_PRIORITY)
            .build();
    executor =
        new ThreadPoolExecutor(
            0,
            Integer.MAX_VALUE,
            30L,
            TimeUnit.SECONDS,
            new SynchronousQueue<Runnable>(),
            threadFactory);
  }

  @Override
  public void start() throws Exception {
    // Nothing to do
  }

  @Override
  public void stop() throws Exception {
    executor.shutdownNow();
    executor.awaitTermination(30, TimeUnit.SECONDS);
  }

  public ExecutorService executor() {
    return executor;
  }
}
