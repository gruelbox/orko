package com.gruelbox.orko.docker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import com.google.common.io.Files;

import io.dropwizard.configuration.ConfigurationSourceProvider;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;

public class TestDockerSecretLookup {

  @Test
  public void testNotStrict() throws IOException {
    File tempDir = Files.createTempDir();
    try {
      DockerSecretLookup notStrict = new DockerSecretLookup(tempDir.getAbsolutePath(), false);
      File file = new File(tempDir.getAbsolutePath() + File.separator + "secret-thing");
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
      File file = new File(tempDir.getAbsolutePath() + File.separator + "secret-thing");
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
  public void testErrorCase1() {
    File tempDir = Files.createTempDir();
    try {
      DockerSecretLookup notStrict = new DockerSecretLookup(tempDir.getAbsolutePath(), false);
      assertEquals(null, notStrict.lookup("evil/../path"));
    } finally {
      tempDir.delete();
    }
  }

  @Test
  public void testIntegration() throws IOException {
    String config = "secret: '${SIMPLE_AUTH_SECRET:-XXX}${secret-jwt-signing-key}'";
    ConfigurationSourceProvider provider = path -> new ByteArrayInputStream(config.getBytes(StandardCharsets.UTF_8));

    DockerSecretLookup lookup = mock(DockerSecretLookup.class);
    SubstitutingSourceProvider output = new SubstitutingSourceProvider(
      new SubstitutingSourceProvider(provider, new EnvironmentVariableSubstitutor(false)),
      new DockerSecretSubstitutor(lookup, false, true)
    );

    assertEquals("secret: 'XXX${secret-jwt-signing-key}'", IOUtils.toString(output.open("whatever"), StandardCharsets.UTF_8));

    when(lookup.lookup("secret-jwt-signing-key")).thenReturn("");
    assertEquals("secret: 'XXX'", IOUtils.toString(output.open("whatever"), StandardCharsets.UTF_8));

    when(lookup.lookup("secret-jwt-signing-key")).thenReturn("STUFF");
    assertEquals("secret: 'XXXSTUFF'", IOUtils.toString(output.open("whatever"), StandardCharsets.UTF_8));
  }
}
