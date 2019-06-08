/**
 * Orko
 * Copyright © 2018-2019 Graham Crockford
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.gruelbox.orko.app.monolith;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.security.Permission;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestCommandLine {

  @Before
  public void setUp() throws Exception {
    System.setSecurityManager(new NoExitSecurityManager());
  }

  @After
  public void tearDown() throws Exception {
    System.setSecurityManager(null);
  }

  @Test
  public void testSalt() throws Exception {
    String result = captureStdOut(() -> MonolithApplication.main("salt"));
    assertTrue(result.length() > 3);
  }

  @Test
  public void testHashNoSalt() throws Exception {
    String result = captureStdOut(() -> MonolithApplication.main("hash", "rhubarb"));
    assertTrue(result.contains("Salt used: "));
    assertTrue(result.contains("Hashed result: HASH("));
  }

  @Test
  public void testHashWithSaltLongArg() throws Exception {
    String salt = "bIR3DvCFPKLfY410OR2u5g==";
    String result = captureStdOut(() -> MonolithApplication.main("hash", "--salt", salt, "porky pig the beast"));
    assertTrue(result.equals("HASH(9NBwT2wpCuA2bCNw8d6JqRIVoxN+GOQzC4PgoH6I4j0=)"));
  }

  @Test
  public void testHashWithSaltShortArg() throws Exception {
    String salt = "bIR3DvCFPKLfY410OR2u5g==";
    String result = captureStdOut(() -> MonolithApplication.main("hash", "-s", salt, "porky pig the beast"));
    assertTrue(result.equals("HASH(9NBwT2wpCuA2bCNw8d6JqRIVoxN+GOQzC4PgoH6I4j0=)"));
  }

  @Test
  public void testOtp() throws Exception {
    String result = captureStdOut(() -> MonolithApplication.main("otp", "--nocheck"));
    assertTrue(result.length() > 3);
  }

  private String captureStdOut(ExceptionThrowingRunnable runnable) throws Exception {
    PrintStream old = System.out;
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); PrintStream ps = new PrintStream(baos)) {
      System.setOut(ps);
      runnable.run();
      System.out.flush();
      String captured = baos.toString();
      old.println(captured);
      return captured;
    } finally {
      System.setOut(old);
    }
  }

  private interface ExceptionThrowingRunnable {
    void run() throws Exception;
  }

  protected static class ExitException extends SecurityException {
    private static final long serialVersionUID = -7617077452732839957L;
    public final int status;

    public ExitException(int status) {
      super("Command exited");
      this.status = status;
    }
  }

  private static class NoExitSecurityManager extends SecurityManager {
    @Override
    public void checkPermission(Permission perm) {
      // allow anything.
    }

    @Override
    public void checkPermission(Permission perm, Object context) {
      // allow anything.
    }

    @Override
    public void checkExit(int status) {
      super.checkExit(status);
      throw new ExitException(status);
    }
  }
}
