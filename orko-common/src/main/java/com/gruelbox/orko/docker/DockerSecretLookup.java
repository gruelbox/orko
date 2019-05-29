package com.gruelbox.orko.docker;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.text.StrLookup;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.io.Files;

import io.dropwizard.configuration.UndefinedEnvironmentVariableException;

/**
 * A custom {@link org.apache.commons.text.StrLookup} implementation using
 * Docker secrets as lookup source.
 */
class DockerSecretLookup extends StrLookup<Object> {

  private final boolean strict;
  private final boolean enabled;
  private final String path;

  /**
   * Create a new instance.
   *
   * @param strict {@code true} if looking up undefined environment variables
   *               should throw a {@link UndefinedEnvironmentVariableException},
   *               {@code false} otherwise.
   * @throws UndefinedEnvironmentVariableException if the environment variable
   *                                               doesn't exist and strict
   *                                               behavior is enabled.
   */
  DockerSecretLookup(boolean strict) {
    this("/run/secrets", strict);
  }

  @VisibleForTesting
  DockerSecretLookup(String path, boolean strict) {
    this.path = path;
    this.enabled = new File(path).exists();
    this.strict = strict;
  }

  /**
   * {@inheritDoc}
   *
   * @throws IllegalArgumentException if the Docker secret doesn't exist and
   *                                  strict behavior is enabled.
   */
  @Override
  public String lookup(String key) {
    if (!enabled && !strict) {
      return null;
    }
    Preconditions.checkArgument(!key.contains(File.separator) && !key.contains("/"), "Path separator in variable name");
    File file = new File(path + File.separator + key);
    String value = null;
    if (file.exists()) {
      try {
        value = Files.asCharSource(file, StandardCharsets.UTF_8).read();
      } catch (IOException e) {
        throw new RuntimeException("IOException when scanning for " + key, e);
      }
    }
    if (value == null && strict) {
      throw new IllegalArgumentException("Docker secret for '" + key
          + "' is not defined; could not substitute the expression '${" + key + "}'.");
    }
    if (value == null && key.startsWith("secret-")) {
      return "";
    }
    return value;
  }
}