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
package com.gruelbox.orko;

import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.common.util.concurrent.Service;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import com.gruelbox.tools.dropwizard.guice.Configured;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Adds support for terminating this process if a parent process dies. */
public class ChildProcessSupportModule extends AbstractModule
    implements Configured<BaseApplicationConfiguration> {

  public static final int KEEP_ALIVE_BYTE = 100;
  public static final int KEEP_ALIVE_TIMEOUT = 10;

  private BaseApplicationConfiguration configuration;

  @Override
  public void setConfiguration(BaseApplicationConfiguration configuration) {
    this.configuration = configuration;
  }

  @Override
  protected void configure() {
    if (configuration.isChildProcess()) {
      Multibinder.newSetBinder(binder(), Service.class)
          .addBinding()
          .to(ChildProcessMonitorProcess.class);
    }
  }

  @Singleton
  private static final class ChildProcessMonitorProcess extends AbstractExecutionThreadService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChildProcessMonitorProcess.class);

    private final Provider<Server> server;
    private final ExecutorService executorService;

    private byte[] buffer = new byte[1];
    private Stopwatch heartbeatTimeout = Stopwatch.createUnstarted();

    @Inject
    ChildProcessMonitorProcess(Provider<Server> server, ExecutorService executorService) {
      this.server = server;
      this.executorService = executorService;
    }

    @Override
    protected void run() throws Exception {
      LOGGER.debug("Watching parent process in case it dies");
      try {
        heartbeatTimeout.start();
        byte[] buffer = new byte[1];
        outerLoop:
        do {
          if (heartbeatTimeout.elapsed(TimeUnit.SECONDS) > KEEP_ALIVE_TIMEOUT) {
            LOGGER.warn("No heartbeat from parent process. Shutting down");
            forceExit();
            break outerLoop;
          }
          int bytesRead = System.in.read(buffer, 0, Math.min(1, System.in.available()));
          switch (bytesRead) {
            case -1:
              // Stream closed
              LOGGER.warn("Input stream closed. Shutting down");
              forceExit();
              break outerLoop;
            case 0:
              // Nothing to read, so wait a bit
              LOGGER.debug("No input. Waiting");
              Thread.sleep(2000);
              break;
            case 1:
              // Check for our keepalive
              if (buffer[0] == KEEP_ALIVE_BYTE) {
                LOGGER.debug("Got keepalive from parent process");
                heartbeatTimeout.reset();
                heartbeatTimeout.start();
              } else {
                LOGGER.debug("Ignored unexpected input char {}", buffer[0]);
              }
              break;
            default:
              LOGGER.warn("Unexpected number of bytes: {} ", bytesRead);
          }
        } while (true);
      } catch (InterruptedException e) {
        LOGGER.info("Parent thread watcher interrupted");
        Thread.currentThread().interrupt();
      } catch (Exception e) {
        LOGGER.error("Parent thread watcher died. Shutting down", e);
        forceExit();
      }
      LOGGER.info("Exiting parent process watcher");
    }

    private void forceExit() {
      new Thread() {
        @Override
        public void run() {
          try {
            server.get().stop();
          } catch (Exception e) {
            LOGGER.error("Failed triggering shutdown", e);
          }
        }
      }.start();
    }
  }
}
