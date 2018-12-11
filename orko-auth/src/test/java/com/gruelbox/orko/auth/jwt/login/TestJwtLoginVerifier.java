package com.gruelbox.orko.auth.jwt.login;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import com.gruelbox.orko.auth.GenerateSecretKey;
import com.gruelbox.orko.auth.Hasher;
import com.gruelbox.orko.auth.jwt.JwtConfiguration;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig;
import com.warrenstrange.googleauth.IGoogleAuthenticator;

import io.dropwizard.auth.AuthenticationException;

public class TestJwtLoginVerifier {

  private final IGoogleAuthenticator googleAuthenticator = new GoogleAuthenticator(
    new GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder().build()
  );

  private final GenerateSecretKey generateSecretKey = new GenerateSecretKey(googleAuthenticator);

  private final JwtConfiguration config = new JwtConfiguration();
  private JwtLoginVerifier verifier;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
    config.setExpirationMinutes(1);
    config.setPasswordSalt("kzn6aaEk2goQpmM463BEUQ==");
    verifier = new JwtLoginVerifier(config, googleAuthenticator, new Hasher());
  }

  @Test
  public void testUnhashedNoSecondFactor() throws AuthenticationException {
    config.setUserName("joe");
    config.setPassword("tester");
    assertFalse(verifier.authenticate(new LoginRequest("j0e", "tester", null)).isPresent());
    assertFalse(verifier.authenticate(new LoginRequest("joe", "t3ster", null)).isPresent());
    assertTrue(verifier.authenticate(new LoginRequest("joe", "tester", null)).isPresent());
  }

  @Test
  public void testHashedNoSecondFactor() throws AuthenticationException {
    config.setUserName("joe");
    config.setPassword("HASH(yxnd7ycRqxkqLMUYqsD8xN49fsDI3snc00fijIdTXVc=)");
    assertTrue(verifier.authenticate(new LoginRequest("joe", "tester", null)).isPresent());
    assertFalse(verifier.authenticate(new LoginRequest("joe", "HASH(yxnd7ycRqxkqLMUYqsD8xN49fsDI3snc00fijIdTXVc=)", null)).isPresent());
  }

  @Test
  public void testUnhashedSecondFactor() throws AuthenticationException {
    config.setUserName("joe");
    config.setPassword("tester");
    config.setSecret("ksdhflsdhfoliDSFSDFdjfp93ur3piruj3pf");
    config.setSecondFactorSecret(generateSecretKey.createNewKey());
    assertFalse(
      verifier.authenticate(
        new LoginRequest(
          "joe",
          "tester",
          generateSecretKey.generateValidInput(config.getSecondFactorSecret()) + 1
        )
      ).isPresent()
    );
    assertTrue(
        verifier.authenticate(
          new LoginRequest(
            "joe",
            "tester",
            generateSecretKey.generateValidInput(config.getSecondFactorSecret())
          )
        ).isPresent()
      );
  }

  @Test
  public void testHashedSecondFactor() throws AuthenticationException {
    config.setUserName("joe");
    config.setPassword("HASH(yxnd7ycRqxkqLMUYqsD8xN49fsDI3snc00fijIdTXVc=)");
    config.setSecret("ksdhflsdhfoliDSFSDFdjfp93ur3piruj3pf");
    config.setSecondFactorSecret(generateSecretKey.createNewKey());
    assertFalse(
      verifier.authenticate(
        new LoginRequest(
          "joe",
          "tester",
          generateSecretKey.generateValidInput(config.getSecondFactorSecret()) + 1
        )
      ).isPresent()
    );
    assertTrue(
        verifier.authenticate(
          new LoginRequest(
            "joe",
            "tester",
            generateSecretKey.generateValidInput(config.getSecondFactorSecret())
          )
        ).isPresent()
      );
  }
}
