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

import com.google.common.base.Preconditions;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class Hasher {

  public boolean isHash(String storedPassword) {
    return storedPassword != null
        && storedPassword.startsWith("HASH(")
        && storedPassword.endsWith(")")
        && !storedPassword.equals("HASH()");
  }

  public String salt() {
    SecureRandom random = new SecureRandom();
    byte[] salt = new byte[16];
    random.nextBytes(salt);
    return Base64.getEncoder().encodeToString(salt);
  }

  public String hashWithString(String value, String stringSalt) {
    Preconditions.checkNotNull(stringSalt);
    String salt = Base64.getEncoder().encodeToString(stringSalt.getBytes(StandardCharsets.UTF_8));
    return hash(value, salt);
  }

  public String hash(String value, String salt) {
    Preconditions.checkNotNull(value, "Null value cannot be hashed");
    Preconditions.checkNotNull(salt, "Salt not supplied");
    KeySpec spec =
        new PBEKeySpec(value.toCharArray(), Base64.getDecoder().decode(salt), 65536, 256);
    try {
      SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
      byte[] hash = factory.generateSecret(spec).getEncoded();
      return "HASH(" + Base64.getEncoder().encodeToString(hash) + ")";
    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
      throw new RuntimeException(e);
    }
  }
}
