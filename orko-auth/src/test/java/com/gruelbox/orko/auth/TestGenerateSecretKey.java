package com.gruelbox.orko.auth;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.warrenstrange.googleauth.GoogleAuthenticator;

public class TestGenerateSecretKey {

  GenerateSecretKey generateSecretKey;

  @Before
  public void setup() {
    generateSecretKey = new GenerateSecretKey(new GoogleAuthenticator());
  }

  @Test
  public void testGenerateSecretKeyTrue() {
    String key = generateSecretKey.createNewKey();
    assertTrue(generateSecretKey.checkKey(key, generateSecretKey.generateValidInput(key)));
  }
}
