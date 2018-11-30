package com.gruelbox.orko.auth;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.warrenstrange.googleauth.GoogleAuthenticator;

public class TestGenerateSecretKey {
  @Test
  public void testGenerateSecretKeyTrue() {
    GenerateSecretKey generateSecretKey = new GenerateSecretKey(new GoogleAuthenticator());
    String key = generateSecretKey.createNewKey();
    assertTrue(generateSecretKey.checkKey(key, generateSecretKey.generateValidInput(key)));
  }
}
