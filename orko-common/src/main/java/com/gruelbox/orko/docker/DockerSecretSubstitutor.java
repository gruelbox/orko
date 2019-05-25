package com.gruelbox.orko.docker;

import org.apache.commons.text.StrSubstitutor;

import io.dropwizard.configuration.UndefinedEnvironmentVariableException;

/**
 * A custom {@link StrSubstitutor} using Docker secrets as a value source.
 */
public class DockerSecretSubstitutor extends StrSubstitutor {

  public DockerSecretSubstitutor(boolean strict) {
    this(strict, false);
  }

  /**
   * @param strict                  {@code true} if looking up undefined
   *                                environment variables should throw a
   *                                {@link UndefinedEnvironmentVariableException},
   *                                {@code false} otherwise.
   * @param substitutionInVariables a flag whether substitution is done in
   *                                variable names.
   * @see io.dropwizard.configuration.EnvironmentVariableLookup#EnvironmentVariableLookup(boolean)
   * @see org.apache.commons.text.StrSubstitutor#setEnableSubstitutionInVariables(boolean)
   */
  public DockerSecretSubstitutor(boolean strict, boolean substitutionInVariables) {
    super(new DockerSecretLookup(strict));
    this.setEnableSubstitutionInVariables(substitutionInVariables);
  }
}
