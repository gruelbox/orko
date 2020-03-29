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
package com.gruelbox.orko.util;

import com.google.common.base.Throwables;
import java.io.Serializable;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * Checked exceptions are the subject of a never-ending debate in the Java world. If you ever find
 * your code cluttered for no apparently good reason, these utilities may help.
 *
 * <p>Note that <em>checked exceptions exist for a reason</em>, and these utilities are not intended
 * to allow you to forget they exist. Use judiciously to make your code more readable, but don't
 * forget that you're using Java.
 *
 * @author grahamc (Graham Crockford)
 */
public class CheckedExceptions {

  /**
   * Runs the specified {@link Runnable}, wrapping any checked exceptions thrown in a {@link
   * RuntimeException}. These are still thrown, but it is not then necessary to use a {@code
   * try...catch} block around the call or rethrow the checked exception in your method signature.
   *
   * <p>In use, the following:
   *
   * <pre><code>try {
   *   doSomething();
   * } catch (SomeCheckedException e) {
   *   throw new RuntimeException(e);
   * }</code></pre>
   *
   * <p>Can be replaced with:
   *
   * <pre><code>CheckedExceptions.runUnchecked(this::doSomething);</code></pre>
   *
   * <p>Specifically, exceptions are treated as follows:
   *
   * <ul>
   *   <li>Unchecked exceptions are simply rethrown.
   *   <li>If {@link InterruptedException} is thrown, the interrupt flag is reset (so we don't hide
   *       that an interrupt occurred) and the exception is rethrown, wrapped in a {@link
   *       RuntimeException}. <strong>Note</strong> that although this should achieve an interrupt
   *       as intended in most circumstances, if you are writing concurrent code, it is arguable
   *       that there may be much cleaner ways to shut down. Please use with due consideration for
   *       why {@link InterruptedException} exists in the first place!
   *   <li>All other checked exceptions are simply wrapped in a {@link RuntimeException} and
   *       rethrown.
   * </ul>
   *
   * @param runnable The code to run, which may throw checked exceptions.
   */
  public static void runUnchecked(ThrowingRunnable runnable) {
    try {
      runnable.run();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException(e);
    } catch (Exception t) {
      Throwables.throwIfUnchecked(t);
      throw new RuntimeException(t);
    }
  }

  /**
   * Equivalent of {@link #runUnchecked(ThrowingRunnable)}, but runs a {@link Callable}, returning
   * the value returned. See {@link #runUnchecked(ThrowingRunnable)} for full information.
   *
   * @param <T> The return type.
   * @param callable The code to run, which may throw checked exceptions.
   * @return The value returned by <code>callable</code>.
   */
  public static <T> T callUnchecked(Callable<T> callable) {
    try {
      return callable.call();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException(e);
    } catch (Exception t) {
      Throwables.throwIfUnchecked(t);
      throw new RuntimeException(t);
    }
  }

  /**
   * Equivalent of {@link #runUnchecked(ThrowingRunnable)}, but runs a {@link Supplier}, returning
   * the value returned. See {@link #runUnchecked(ThrowingRunnable)} for full information.
   *
   * @param <T> The return type.
   * @param callable The code to run, which may throw checked exceptions.
   * @return The value returned by <code>callable</code>.
   */
  public static <T> T getUnchecked(Callable<T> callable) {
    try {
      return callable.call();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException(e);
    } catch (Exception t) {
      Throwables.throwIfUnchecked(t);
      throw new RuntimeException(t);
    }
  }

  /**
   * Wraps a checked exception-throwing lambda in another lambda which uses {@link
   * #runUnchecked(ThrowingRunnable)} to convert checked exceptions to unchecked exceptions. Can be
   * useful when trying to write lambdas but where the enclosed code throws checked exceptions and
   * the consumer only accepts a conventional {@link FunctionalInterface}.
   *
   * <p>For example, the following deals with the unspecified {@link Exception} thrown by an {@link
   * AutoCloseable} resource used in a try with resources:
   *
   * <pre>executor.execute(CheckedExceptions.uncheck(() -&gt; {
   *  try (Connection c = getDBConnection();
   *    ...
   *  }
   * }));</pre>
   *
   * <p>Note that the lambda is cast at construction time to {@link Serializable} so may be
   * serialised, assuming that you have provided serialisability on any enclosed objects. TODO
   * REALLY NEED TO DOUBLE CHECK THIS. NOT CONVINCED IN THE SLIGHTEST. NEEDS TESTS.
   *
   * @param runnable The lambda to wrap.
   * @return The now non-throwing {@link Runnable}.
   */
  public static Runnable uncheck(ThrowingRunnable runnable) {
    return (Runnable & Serializable) () -> runUnchecked(runnable);
  }

  /**
   * Equivalent of {@link #uncheck(ThrowingRunnable)}, but wraps a {@link Callable}. See {@link
   * #uncheck(ThrowingRunnable)} for more information.
   *
   * <p>The {@link Callable} is effectively converted into a {@link Supplier}.
   *
   * <p>Note that the lambda is cast at construction time to {@link Serializable} so may be
   * serialised, assuming that you have provided serialisability on any enclosed objects..
   *
   * @param <T> The return type.
   * @param callable The lambda to wrap.
   * @return The now non-throwing {@link Supplier}.
   */
  public static <T> Supplier<T> uncheck(Callable<T> callable) {
    return (Supplier<T> & Serializable) () -> callUnchecked(callable);
  }

  /**
   * Functional interface representing a {@link Runnable} which throws a checked {@link Throwable}
   *
   * @author grahamc (Graham Crockford)
   */
  @FunctionalInterface
  public interface ThrowingRunnable {
    public void run() throws Exception;
  }
}
