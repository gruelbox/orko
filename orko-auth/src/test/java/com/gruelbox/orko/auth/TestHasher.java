package com.gruelbox.orko.auth;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import org.junit.Before;
import org.junit.Test;

public class TestHasher {

  Hasher hasher;

  @Before
  public void setup() {
    hasher = new Hasher();
  }

  @Test
  public void testSalt() throws NoSuchAlgorithmException, InvalidKeySpecException {
    hasher.salt();
  }

  @Test
  public void testHash() throws NoSuchAlgorithmException, InvalidKeySpecException {
    assertThat(
        hasher.hash("porky pig the beast", "bIR3DvCFPKLfY410OR2u5g=="),
      equalTo("HASH(9NBwT2wpCuA2bCNw8d6JqRIVoxN+GOQzC4PgoH6I4j0=)")
    );
  }
}
