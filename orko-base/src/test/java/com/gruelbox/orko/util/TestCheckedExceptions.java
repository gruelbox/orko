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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * Tests for {@link CheckedExceptions}.
 *
 * @author grahamc (Graham Crockford)
 */
public class TestCheckedExceptions {

  private static final int VALUE = 5;

  @Mock private Dummy mock;

  @Before
  public void setup() throws SQLException, InterruptedException {
    MockitoAnnotations.initMocks(this);
    when(mock.returnSomething()).thenReturn(VALUE);
    when(mock.returnSomethingWithCheckedException()).thenReturn(VALUE);
    when(mock.returnSomethingWithInterruptedException()).thenReturn(VALUE);
  }

  /**
   * Make sure we can pass a lambda which throws no exceptions and it just acts as a passthrough.
   */
  @Test
  public void testNonThrowingRunnable() {
    CheckedExceptions.runUnchecked(mock::doSomething);
    verify(mock).doSomething();
  }

  /**
   * Make sure we can pass a value-returning lambda which throws no exceptions and it just acts as a
   * passthrough.
   */
  @Test
  public void testNonThrowingCallable() {
    Integer result = CheckedExceptions.callUnchecked(mock::returnSomething);
    assertEquals(VALUE, result.intValue());
  }

  /**
   * Make sure we can pass a lambda which throws a checked exception and we don't need to check the
   * exception
   */
  @Test
  public void testThrowingRunnableOK() {
    CheckedExceptions.runUnchecked(mock::doSomethingWithCheckedException);
    try {
      verify(mock).doSomethingWithCheckedException();
    } catch (IOException e) {
      fail();
    }
  }

  /**
   * Make sure we can pass a value-returning lambda which throws a checked exception and we don't
   * need to check the exception
   */
  @Test
  public void testThrowingCallableOK() {
    Integer result = CheckedExceptions.callUnchecked(mock::returnSomethingWithCheckedException);
    assertEquals(VALUE, result.intValue());
  }

  /**
   * Make sure we can pass a lambda which throws a checked exception and if an exception is thrown,
   * it is wrapped in an unchecked exceptions
   */
  @Test
  public void testThrowingRunnableThrows() {
    try {
      doThrow(IOException.class).when(mock).doSomethingWithCheckedException();
    } catch (IOException e1) {
      fail();
    }
    try {
      CheckedExceptions.runUnchecked(mock::doSomethingWithCheckedException);
    } catch (RuntimeException e) {
      assertEquals(IOException.class, e.getCause().getClass());
      return;
    } catch (Exception e) {
      fail("Wrong exception : " + e);
    }
    fail("No exception");
  }

  /**
   * Make sure we can pass a value-returning lambda which throws a checked exception and if an
   * exception is thrown, it is wrapped in an unchecked exceptions
   */
  @Test
  public void testThrowingCallableThrows() {
    try {
      doThrow(SQLException.class).when(mock).returnSomethingWithCheckedException();
    } catch (SQLException e1) {
      fail();
    }
    try {
      CheckedExceptions.callUnchecked(mock::returnSomethingWithCheckedException);
    } catch (RuntimeException e) {
      assertEquals(SQLException.class, e.getCause().getClass());
      return;
    } catch (Exception e) {
      fail("Wrong exception : " + e);
    }
    fail("No exception");
  }

  /**
   * Make sure we can pass a lambda which throws {@link InterruptedException} without having to
   * rethrow/catch. Such exceptions are wrapped in {@link RuntimeException} but the interrupted flag
   * is preserved.
   */
  @Test
  public void testThrowingRunnableIsInterrupted() {
    try {
      doThrow(InterruptedException.class).when(mock).doSomethingWithInterruptedException();
    } catch (InterruptedException e1) {
      fail();
    }
    try {
      CheckedExceptions.runUnchecked(mock::doSomethingWithInterruptedException);
    } catch (RuntimeException e) {
      assertTrue(Thread.interrupted());
      assertEquals(InterruptedException.class, e.getCause().getClass());
      return;
    } catch (Exception e) {
      fail("Wrong exception : " + e);
    }
    fail("No exception");
  }

  /**
   * Make sure we can pass a value-returning lambda which throws {@link InterruptedException}
   * without having to rethrow/catch. Such exceptions are wrapped in {@link RuntimeException} but
   * the interrupted flag is preserved.
   */
  @Test
  public void testThrowingCallableIsInterrupted() {
    try {
      doThrow(InterruptedException.class).when(mock).returnSomethingWithInterruptedException();
    } catch (InterruptedException e1) {
      fail();
    }
    try {
      CheckedExceptions.callUnchecked(mock::returnSomethingWithInterruptedException);
    } catch (RuntimeException e) {
      assertTrue(Thread.interrupted());
      assertEquals(InterruptedException.class, e.getCause().getClass());
      return;
    } catch (Exception e) {
      fail("Wrong exception : " + e);
    }
    fail("No exception");
  }

  /**
   * Test that we can wrap a lambda which throws exceptions and pass it to a method that doesn't
   * expect them.
   */
  @Test
  public void testHandleCheckedExceptionsInLambda() {

    Mockito.doAnswer(
            invocation -> {
              Runnable argument = invocation.getArgument(0);
              argument.run();
              return null;
            })
        .when(mock)
        .receiveRunnable(Mockito.any(Runnable.class));

    mock.receiveRunnable(
        CheckedExceptions.uncheck(
            () -> {
              mock.doSomethingWithCheckedException();
            }));

    try {
      verify(mock).doSomethingWithCheckedException();
    } catch (IOException e) {
      fail();
    }
  }

  /**
   * Test that we can wrap a lambda which throws exceptions and pass it to a method that doesn't
   * expect them, but the exception is correctly wrapped and rethrown as a {@link RuntimeException}.
   */
  @Test
  public void testThrowCheckedExceptionsInLambda() {

    try {
      doThrow(IOException.class).when(mock).doSomethingWithCheckedException();
    } catch (IOException e1) {
      fail();
    }

    Mockito.doAnswer(
            invocation -> {
              Runnable argument = invocation.getArgument(0);
              argument.run();
              return null;
            })
        .when(mock)
        .receiveRunnable(Mockito.any(Runnable.class));

    try {
      mock.receiveRunnable(
          CheckedExceptions.uncheck(
              () -> {
                mock.doSomethingWithCheckedException();
              }));
    } catch (RuntimeException e) {
      assertEquals(IOException.class, e.getCause().getClass());
      return;
    } catch (Exception e) {
      fail("Wrong exception : " + e);
    }
    fail("No exception");
  }

  private interface Dummy {
    public void doSomething();

    public void doSomethingWithCheckedException() throws IOException;

    public void doSomethingWithInterruptedException() throws InterruptedException;

    public int returnSomething();

    public int returnSomethingWithCheckedException() throws SQLException;

    public int returnSomethingWithInterruptedException() throws InterruptedException;

    public void receiveRunnable(Runnable runnable);

    public void receiveCallable(Callable<?> callable);
  }
}
