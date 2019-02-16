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
    assertTrue(result.startsWith("Salt: "));
  }

  @Test
  public void testHashNoSalt() throws Exception {
    String result = captureStdOut(() -> MonolithApplication.main("hash", "rhubarb"));
    assertTrue(result.contains("Salt used: "));
    assertTrue(result.contains("Hashed result: HASH("));
  }

  @Test
  public void testHashWithSaltLongArg() throws Exception {
    String salt = captureStdOut(() -> MonolithApplication.main("salt")).substring(6);
    String result = captureStdOut(() -> MonolithApplication.main("hash", "--salt", salt, "rhubarb"));
    assertTrue(result.contains("Salt used: " + salt));
    assertTrue(result.contains("Hashed result: HASH("));
  }

  @Test
  public void testHashWithSaltShortArg() throws Exception {
    String salt = captureStdOut(() -> MonolithApplication.main("salt")).substring(6);
    String result = captureStdOut(() -> MonolithApplication.main("hash", "-s", salt, "rhubarb"));
    assertTrue(result.contains("Salt used: " + salt));
    assertTrue(result.contains("Hashed result: HASH("));
  }

  @Test
  public void testOtp() throws Exception {
    String result = captureStdOut(() -> MonolithApplication.main("otp", "--nocheck"));
    assertTrue(result.contains("Here's your key. Enter it into Google Authenticator:"));
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
