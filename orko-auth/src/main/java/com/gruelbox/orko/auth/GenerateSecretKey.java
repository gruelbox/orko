package com.gruelbox.orko.auth;

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
        if (generator.checkKey(key, Integer.parseInt(response))) {
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
