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
package com.gruelbox.orko.auth;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.warrenstrange.googleauth.IGoogleAuthenticator;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

public class GenerateSecretKey {

  public static void main(String... args) throws IOException {
    Injector injector = Guice.createInjector(new GoogleAuthenticatorModule());
    otp(injector.getInstance(GenerateSecretKey.class), !Arrays.asList(args).contains("--nocheck"));
  }

  private static void otp(final GenerateSecretKey generator, boolean doCheck) throws IOException {
    final String key = generator.createNewKey();
    if (doCheck) {
      System.out.println("Here's your key. Enter it into Google Authenticator:");
    }
    displayKey(key);
    if (doCheck) {
      System.out.println("");
      checkKey(generator, key);
    }
  }

  private static void displayKey(final String key) {
    System.out.print(key);
  }

  private static void checkKey(final GenerateSecretKey generator, final String key)
      throws IOException {
    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(System.in, Charsets.UTF_8))) {
      String response = null;
      while (response == null || !generator.checkKey(key, response)) {
        if (response == null) {
          System.out.println("Now confirm the current value from Google Authenticator:");
        } else {
          System.out.println("Invalid input. Try again.");
        }
        response = reader.readLine();
      }
      System.out.println("Correct.");
    }
  }

  private final IGoogleAuthenticator googleAuthenticator;

  @Inject
  public GenerateSecretKey(IGoogleAuthenticator googleAuthenticator) {
    this.googleAuthenticator = googleAuthenticator;
  }

  @VisibleForTesting
  public String createNewKey() {
    return googleAuthenticator.createCredentials().getKey();
  }

  @VisibleForTesting
  public int generateValidInput(String key) {
    return googleAuthenticator.getTotpPassword(key);
  }

  @VisibleForTesting
  private boolean checkKey(String key, String value) {
    try {
      return googleAuthenticator.authorize(key, Integer.parseInt(value));
    } catch (Exception e) {
      System.out.println(e);
      return false;
    }
  }

  @VisibleForTesting
  public boolean checkKey(String key, int value) {
    return googleAuthenticator.authorize(key, value);
  }
}
