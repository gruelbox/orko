package com.grahamcrockford.orko.web.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import com.google.inject.Guice;
import com.grahamcrockford.orko.auth.AuthModule;
import com.warrenstrange.googleauth.GoogleAuthenticator;

public class Test2Fa {

  public static void main(String[] args) throws IOException {

    GoogleAuthenticator auth = Guice.createInjector(new AuthModule())
      .getInstance(GoogleAuthenticator.class);

    try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
      System.out.println("Enter your 2FA secret");
      String secret = reader.readLine();
      System.out.println("What does your phone say?");
      String response = reader.readLine();

      if (auth.authorize(secret, Integer.valueOf(response))) {
        System.out.println("All good");
      } else {
        System.out.println("Nope");
      }
    }
  }
}
