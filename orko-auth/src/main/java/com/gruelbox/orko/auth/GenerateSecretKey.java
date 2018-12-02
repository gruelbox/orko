package com.gruelbox.orko.auth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.warrenstrange.googleauth.IGoogleAuthenticator;

public class GenerateSecretKey {

  public static void main(String[] args) throws IOException {

    final GenerateSecretKey generator = Guice.createInjector(new GoogleAuthenticatorModule()).getInstance(GenerateSecretKey.class);

    final String key = generator.createNewKey();

    System.out.println("Here's your key. Enter it into Google Authenticator:");
    System.out.println(key);

    while (true) {
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, Charsets.UTF_8))) {
        System.out.println("Now enter the current value from Google Authenticator:");
        String response = reader.readLine();
        if (generator.checkKey(key, response)) {
          System.out.println("Yep, that's working.");
          System.exit(0);
        } else {
          System.out.println("Something's wrong. Try again.");
        }
      }
    }
  }

  private final IGoogleAuthenticator googleAuthenticator;

  @Inject
  GenerateSecretKey(IGoogleAuthenticator googleAuthenticator) {
    this.googleAuthenticator = googleAuthenticator;
  }

  @VisibleForTesting
  String createNewKey() {
    return googleAuthenticator.createCredentials().getKey();
  }

  @VisibleForTesting
  String generateValidInput(String key) {
    return Integer.toString(googleAuthenticator.getTotpPassword(key));
  }

  @VisibleForTesting
  boolean checkKey(String key, String value) {
    return googleAuthenticator.authorize(key, Integer.parseInt(value));
  }
}
