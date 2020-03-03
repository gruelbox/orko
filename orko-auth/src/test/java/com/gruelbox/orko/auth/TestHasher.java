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

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

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
  public void testIsHash() {
    assertTrue(hasher.isHash("HASH(YES)"));
    assertFalse(hasher.isHash("HASH(NOTAHASH"));
    assertFalse(hasher.isHash("NOTAHASH)"));
    assertFalse(hasher.isHash("hash(NOTAHASH)"));
    assertFalse(hasher.isHash("NOTAHASH"));
    assertFalse(hasher.isHash("HASH()"));
    assertFalse(hasher.isHash(null));
  }

  @Test
  public void testSalt() throws NoSuchAlgorithmException, InvalidKeySpecException {
    hasher.salt();
  }

  @Test
  public void testHash() throws NoSuchAlgorithmException, InvalidKeySpecException {
    assertThat(
        hasher.hash("porky pig the beast", "bIR3DvCFPKLfY410OR2u5g=="),
        equalTo("HASH(9NBwT2wpCuA2bCNw8d6JqRIVoxN+GOQzC4PgoH6I4j0=)"));
  }
}
