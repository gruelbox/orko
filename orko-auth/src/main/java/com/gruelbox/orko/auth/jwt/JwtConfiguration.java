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
package com.gruelbox.orko.auth.jwt;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import java.nio.charset.StandardCharsets;
import org.apache.commons.lang3.StringUtils;

public class JwtConfiguration {

  @JsonProperty private String secret;
  @JsonProperty private String userName;
  @JsonProperty private String password;
  @JsonProperty private String passwordSalt;
  @JsonProperty private String secondFactorSecret;
  @JsonProperty private int expirationMinutes = 60 * 24;

  public String getSecret() {
    return secret;
  }

  public void setSecret(String secret) {
    this.secret = secret;
  }

  public byte[] getSecretBytes() {
    Preconditions.checkNotNull(secret);
    return secret.getBytes(StandardCharsets.UTF_8);
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getPasswordSalt() {
    return passwordSalt;
  }

  public void setPasswordSalt(String passwordSalt) {
    this.passwordSalt = passwordSalt;
  }

  public int getExpirationMinutes() {
    return expirationMinutes;
  }

  public void setExpirationMinutes(int expirationMinutes) {
    this.expirationMinutes = expirationMinutes;
  }

  public String getSecondFactorSecret() {
    return secondFactorSecret;
  }

  public void setSecondFactorSecret(String secondFactorSecret) {
    this.secondFactorSecret = secondFactorSecret;
  }

  public boolean isEnabled() {
    return StringUtils.isNotEmpty(secret);
  }
}
