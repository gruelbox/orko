package com.gruelbox.orko.auth;

/*-
 * ===============================================================================L
 * Orko Auth
 * ================================================================================
 * Copyright (C) 2018 - 2019 Graham Crockford
 * ================================================================================
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * ===============================================================================E
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.warrenstrange.googleauth.IGoogleAuthenticator;

public class GenerateSecretKey {

  public static void main(String[] args) throws IOException {
    Injector injector = Guice.createInjector(new GoogleAuthenticatorModule());
    otp(injector.getInstance(GenerateSecretKey.class));
  }

  private static void otp(final GenerateSecretKey generator) throws IOException {
    final String key = generator.createNewKey();
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, Charsets.UTF_8))) {
      while (true) {
        System.out.println("Here's your key. Enter it into Google Authenticator:");
        System.out.println(key);
        System.out.println("Now enter the current value from Google Authenticator:");
        String response = reader.readLine();
        if (response != null && generator.checkKey(key, Integer.parseInt(response))) {
          System.out.println("Yep, that's working.");
          break;
        } else {
          System.out.println("Something's wrong. Try again.");
        }
      }
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
  public  boolean checkKey(String key, int value) {
    return googleAuthenticator.authorize(key, value);
  }
}
