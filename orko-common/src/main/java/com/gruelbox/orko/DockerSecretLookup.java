package com.gruelbox.orko;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.text.StrLookup;

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
    this.enabled = new File("/run/secrets").exists();
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
    if (!enabled && !strict)
      return null;
    Preconditions.checkArgument(!key.contains("/"), "Path separator in variable name");
    try {
      return Files.asCharSource(new File("/run/secrets/" + key), StandardCharsets.UTF_8).read();
    } catch (IOException e) {
      if (strict) {
        throw new IllegalArgumentException("Docker secret for '" + key
            + "' is not defined; could not substitute the expression '${" + key + "}'.", e);
      } else {
        return null;
      }
    }
  }
}