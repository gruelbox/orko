package com.gruelbox.orko.auth.jwt.login;

/*-
 * ===============================================================================L
 * Orko Auth
 * ================================================================================
 * Copyright (C) 2018 - 2019 Graham Crockford
 * ================================================================================
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * ===============================================================================E
 */

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class LoginRequest {

  private final String username;
  private final String password;
  private final Integer secondFactor;

  @JsonCreator
  public LoginRequest(@JsonProperty("username") String username,
                      @JsonProperty("password") String password,
                      @JsonProperty("secondFactor") Integer secondFactor) {
    this.secondFactor = secondFactor;
    this.username = requireNonNull(username);
    this.password = requireNonNull(password);
  }

  @JsonProperty
  public String getUsername() {
      return username;
  }

  @JsonProperty
  public String getPassword() {
      return password;
  }

  @JsonProperty
  public Integer getSecondFactor() {
    return secondFactor;
  }
}
