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
package com.gruelbox.orko.docker;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.io.Files;
import io.dropwizard.configuration.ConfigurationSourceProvider;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.hamcrest.Matchers;
import org.junit.Test;

public class TestDockerSecretLookup {

  @Test
  public void testDummyBlank() throws IOException {
    File tempDir = Files.createTempDir();
    try {
      DockerSecretLookup notStrict = new DockerSecretLookup(tempDir.getAbsolutePath(), false);
      File file = new File(tempDir.getAbsolutePath() + File.separator + "secret-thing");
      file.createNewFile();
      try {
        Files.asCharSink(file, StandardCharsets.UTF_8).write(DockerSecretLookup.BLANK);
        assertEquals("", notStrict.lookup("secret-thing"));
      } finally {
        file.delete();
      }
      assertThat(
          notStrict.getLog(),
          contains(
              "Docker secrets enabled = true",
              "Found value for secret-thing (length=7)",
              " - treating as blank"));
    } finally {
      tempDir.delete();
    }
  }

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
      assertThat(
          notStrict.getLog(),
          contains("Docker secrets enabled = true", "Found value for secret-thing (length=7)"));
    } finally {
      tempDir.delete();
    }
  }

  @Test
  public void testNotStrictMissingDir() throws IOException {
    DockerSecretLookup notStrict = new DockerSecretLookup("here", false);
    assertEquals(null, notStrict.lookup("thing"));
    assertThat(notStrict.getLog(), Matchers.contains("Docker secrets enabled = false"));
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
      assertThat(
          strict.getLog(),
          contains("Docker secrets enabled = true", "Found value for secret-thing (length=7)"));
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
    ConfigurationSourceProvider provider =
        path -> new ByteArrayInputStream(config.getBytes(StandardCharsets.UTF_8));

    DockerSecretLookup lookup = mock(DockerSecretLookup.class);
    SubstitutingSourceProvider output =
        new SubstitutingSourceProvider(
            new SubstitutingSourceProvider(provider, new EnvironmentVariableSubstitutor(false)),
            new DockerSecretSubstitutor(lookup, false, true));

    assertEquals(
        "secret: 'XXX${secret-jwt-signing-key}'",
        IOUtils.toString(output.open("whatever"), StandardCharsets.UTF_8));

    when(lookup.lookup("secret-jwt-signing-key")).thenReturn("");
    assertEquals(
        "secret: 'XXX'", IOUtils.toString(output.open("whatever"), StandardCharsets.UTF_8));

    when(lookup.lookup("secret-jwt-signing-key")).thenReturn("STUFF");
    assertEquals(
        "secret: 'XXXSTUFF'", IOUtils.toString(output.open("whatever"), StandardCharsets.UTF_8));
  }
}
