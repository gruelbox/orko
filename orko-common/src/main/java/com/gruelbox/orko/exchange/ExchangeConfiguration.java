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
package com.gruelbox.orko.exchange;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;

public class ExchangeConfiguration {

  private String userName;
  private String secretKey;
  private String apiKey;
  private String passphrase;
  private boolean sandbox;
  private boolean loadRemoteData = true;
  private boolean enabled = true;

  @JsonProperty
  public String getUserName() {
    return userName;
  }

  @JsonProperty
  public String getSecretKey() {
    return secretKey;
  }

  @JsonProperty
  public String getApiKey() {
    return apiKey;
  }

  @JsonProperty
  public String getPassphrase() {
    return passphrase;
  }

  @JsonProperty
  public boolean isSandbox() {
    return sandbox;
  }

  @JsonProperty
  public boolean isLoadRemoteData() {
    return loadRemoteData;
  }

  @JsonProperty
  public boolean isEnabled() {
    return enabled;
  }

  @JsonProperty
  public void setUserName(String userName) {
    this.userName = userName;
  }

  @JsonProperty
  public void setSecretKey(String secretKey) {
    this.secretKey = secretKey;
  }

  @JsonProperty
  public void setApiKey(String apiKey) {
    this.apiKey = apiKey;
  }

  @JsonProperty
  public void setPassphrase(String passphrase) {
    this.passphrase = passphrase;
  }

  @JsonProperty
  public void setSandbox(boolean sandbox) {
    this.sandbox = sandbox;
  }

  @JsonProperty
  public void setLoadRemoteData(boolean loadRemoteData) {
    this.loadRemoteData = loadRemoteData;
  }

  @JsonProperty
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public boolean isAuthenticated() {
    return StringUtils.isNotEmpty(apiKey);
  }
}
