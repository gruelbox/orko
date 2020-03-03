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
package com.gruelbox.orko.integration;

import com.gruelbox.orko.ChildProcessSupportModule;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Fork {

  private static final Logger LOGGER = LoggerFactory.getLogger(Fork.class);

  public static Process exec(Class<?> clazz, String... args)
      throws IOException, InterruptedException {
    String javaHome = System.getProperty("java.home");
    String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
    String classPath = System.getProperty("java.class.path");

    ArrayList<String> allArgs = new ArrayList<>(4 + args.length);
    allArgs.add(javaBin);
    allArgs.add("-cp");
    allArgs.add(classPath);
    allArgs.add(clazz.getName());
    allArgs.addAll(Arrays.asList(args));

    Process process =
        new ProcessBuilder(allArgs.toArray(new String[0])).redirectErrorStream(true).start();

    return process;
  }

  public static Future<?> keepAlive(Process process, ExecutorService executorService) {
    return executorService.submit(
        () -> {
          LOGGER.debug("Keepalive process starting: {}", process.pid());
          try {
            while (process.isAlive()) {
              LOGGER.debug("Sending keepalive to: {}", process.pid());
              process.getOutputStream().write(ChildProcessSupportModule.KEEP_ALIVE_BYTE);
              process.getOutputStream().flush();
              Thread.sleep((ChildProcessSupportModule.KEEP_ALIVE_TIMEOUT / 2) * 1000);
            }
            LOGGER.debug("Keepalive process shutting down; process stopped: {}", process.pid());
          } catch (InterruptedException e) {
            LOGGER.debug("Keepalive process shut down");
          } catch (Exception e) {
            LOGGER.error("Keepalive process failed: {}", process.pid(), e);
          }
          return null;
        });
  }

  public static Future<?> pipeOutput(
      Process process, OutputStream outputStream, ExecutorService executorService) {
    return executorService.submit(
        () -> {
          LOGGER.info("Pipe process starting: {}", process.pid());
          try {
            IOUtils.copy(process.getInputStream(), System.out);
            LOGGER.debug("Pipe process completed: {}", process.pid());
          } catch (Exception e) {
            LOGGER.error("Pipe process failed: {}", process.pid(), e);
          }
          return null;
        });
  }
}
