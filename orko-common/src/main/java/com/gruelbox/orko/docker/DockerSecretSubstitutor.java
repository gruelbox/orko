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
package com.gruelbox.orko.docker;

import org.apache.commons.text.StrSubstitutor;

import com.google.common.annotations.VisibleForTesting;

import io.dropwizard.configuration.UndefinedEnvironmentVariableException;

/**
 * A custom {@link StrSubstitutor} using Docker secrets as a value source.
 */
public class DockerSecretSubstitutor extends StrSubstitutor {

  /**
   * @param strict                  {@code true} if looking up undefined
   *                                environment variables should throw a
   *                                {@link UndefinedEnvironmentVariableException},
   *                                {@code false} otherwise.
   * @param substitutionInVariables a flag whether substitution is done in
   *                                variable names.
   * @param escapeYaml              indicates whether special YAML characters should be escaped.
   * @see io.dropwizard.configuration.EnvironmentVariableLookup#EnvironmentVariableLookup(boolean)
   * @see org.apache.commons.text.StrSubstitutor#setEnableSubstitutionInVariables(boolean)
   */
  public DockerSecretSubstitutor(boolean strict, boolean substitutionInVariables, boolean escapeYaml) {
    this(new DockerSecretLookup(strict), substitutionInVariables, escapeYaml);
  }

  @VisibleForTesting
  DockerSecretSubstitutor(DockerSecretLookup lookup, boolean substitutionInVariables, boolean escapeYaml) {
    super(
        escapeYaml
          ? new YamlEscapingStrLookupAdapter<>(lookup)
          : lookup
      );
      this.setEnableSubstitutionInVariables(substitutionInVariables);
  }
}
