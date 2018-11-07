package com.grahamcrockford.orko.auth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.google.inject.Guice;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.IGoogleAuthenticator;

public class GenerateSecretKey {

  public static void main(String[] args) throws IOException {

    final IGoogleAuthenticator auth = Guice.createInjector(new GoogleAuthenticatorModule())
      .getInstance(IGoogleAuthenticator.class);

    final GoogleAuthenticatorKey key = auth.createCredentials();

    System.out.println("Here's your key. Enter it into Google Authenticator:");
    System.out.println(key.getKey());

    while (true) {
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
        System.out.println("Now enter the current value from Google Authenticator:");
        String response = reader.readLine();
        if (auth.authorize(key.getKey(), Integer.valueOf(response))) {
          System.out.println("Yep, that's working.");
          System.exit(0);
        } else {
          System.out.println("Something's wrong. Try again.");
        }
      }
    }
  }
}
