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
package com.gruelbox.orko.auth.jwt.login;

import static org.jose4j.jws.AlgorithmIdentifiers.HMAC_SHA256;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.gruelbox.orko.auth.AuthConfiguration;
import io.dropwizard.auth.PrincipalImpl;
import java.util.Arrays;
import java.util.UUID;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.keys.HmacKey;

public class TokenIssuer {

  public static final String XSRF_CLAIM = "xsrf";

  private final byte[] secret;
  private final int expiry;

  @Inject
  TokenIssuer(AuthConfiguration authConfiguration) {
    this.secret = authConfiguration.getJwt().getSecretBytes();
    this.expiry = authConfiguration.getJwt().getExpirationMinutes();
  }

  @VisibleForTesting
  public TokenIssuer(byte[] secret, int expiry) {
    this.secret = Arrays.copyOf(secret, secret.length);
    this.expiry = expiry;
  }

  public JsonWebSignature buildToken(PrincipalImpl user, String roles) {
    return claimsToToken(buildClaims(user, roles));
  }

  public JwtClaims buildClaims(PrincipalImpl user, String roles) {
    final JwtClaims claims = new JwtClaims();
    claims.setSubject(user.getName());
    claims.setStringClaim("roles", roles);
    claims.setStringClaim(XSRF_CLAIM, UUID.randomUUID().toString());
    claims.setExpirationTimeMinutesInTheFuture(expiry);
    claims.setIssuedAtToNow();
    claims.setGeneratedJwtId();
    return claims;
  }

  public JsonWebSignature claimsToToken(final JwtClaims claims) {
    final JsonWebSignature jws = new JsonWebSignature();
    jws.setPayload(claims.toJson());
    jws.setAlgorithmHeaderValue(HMAC_SHA256);
    jws.setKey(new HmacKey(secret));
    return jws;
  }
}
