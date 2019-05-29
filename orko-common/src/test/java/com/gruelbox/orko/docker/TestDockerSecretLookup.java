package com.gruelbox.orko.docker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

import com.google.common.io.Files;

public class TestDockerSecretLookup {

  @Test
  public void testNotStrict() throws IOException {
    File tempDir = Files.createTempDir();
    try {
      DockerSecretLookup notStrict = new DockerSecretLookup(tempDir.getAbsolutePath(), false);
      File file = new File(tempDir.getAbsolutePath() + File.pathSeparator + "secret-thing");
      file.createNewFile();
      try {
        Files.asCharSink(file, StandardCharsets.UTF_8).write("CONTENT");
        assertEquals("CONTENT", notStrict.lookup("secret-thing"));
        assertEquals(null, notStrict.lookup("thing"));
        assertEquals("", notStrict.lookup("secret-thing2"));
      } finally {
        file.delete();
      }
    } finally {
      tempDir.delete();
    }
  }

  @Test
  public void testNotStrictMissingDir() throws IOException {
    DockerSecretLookup notStrict = new DockerSecretLookup("here", false);
    assertEquals(null, notStrict.lookup("thing"));
  }

  @Test
  public void testStrict() throws IOException {
    File tempDir = Files.createTempDir();
    try {
      DockerSecretLookup strict = new DockerSecretLookup(tempDir.getAbsolutePath(), true);
      File file = new File(tempDir.getAbsolutePath() + File.pathSeparator + "secret-thing");
      file.createNewFile();
      try {
        Files.asCharSink(file, StandardCharsets.UTF_8).write("CONTENT");
        assertEquals("CONTENT", strict.lookup("secret-thing"));
        try {
          strict.lookup("thing");
          fail();
        } catch (IllegalArgumentException e) {
          // OK
        }
        try {
          strict.lookup("secret-thing2");
          fail();
        } catch (IllegalArgumentException e) {
          // OK
        }
      } finally {
        file.delete();
      }
    } finally {
      tempDir.delete();
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void testStrictMissingDir() throws IOException {
    DockerSecretLookup notStrict = new DockerSecretLookup("here", true);
    assertEquals(null, notStrict.lookup("thing"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testErrorCase() {
    File tempDir = Files.createTempDir();
    try {
      DockerSecretLookup notStrict = new DockerSecretLookup(tempDir.getAbsolutePath(), false);
      assertEquals(null, notStrict.lookup("evil/../path"));
    } finally {
      tempDir.delete();
    }
  }
}
