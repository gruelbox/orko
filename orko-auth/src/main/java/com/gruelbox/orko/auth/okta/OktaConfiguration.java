/**
 * Orko
 * Copyright © 2018-2019 Graham Crockford
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.gruelbox.orko.auth.okta;

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

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OktaConfiguration {

  @NotNull
  private String baseUrl;

  @NotNull
  private String clientId;

  @NotNull
  private String issuer;

  private String audience;

  @JsonProperty
  String getBaseUrl() {
    return baseUrl;
  }

  @JsonProperty
  void setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  @JsonProperty
  String getClientId() {
    return clientId;
  }

  @JsonProperty
  void setClientId(String clientId) {
    this.clientId = clientId;
  }

  @JsonProperty
  String getIssuer() {
    return issuer;
  }

  @JsonProperty
  void setIssuer(String issuer) {
    this.issuer = issuer;
  }

  @JsonProperty
  String getAudience() {
    return audience;
  }

  @JsonProperty
  void setAudience(String audience) {
    this.audience = audience;
  }

  public boolean isEnabled() {
    return StringUtils.isNotEmpty(issuer);
  }
}
