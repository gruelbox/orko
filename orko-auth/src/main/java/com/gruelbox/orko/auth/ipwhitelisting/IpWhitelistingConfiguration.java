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
package com.gruelbox.orko.auth.ipwhitelisting;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;

public class IpWhitelistingConfiguration {

  private String secretKey;
  private int whitelistExpirySeconds = 28800;

  @JsonProperty
  String getSecretKey() {
    return secretKey;
  }

  @JsonProperty
  void setSecretKey(String secretKey) {
    this.secretKey = secretKey;
  }

  @JsonProperty
  public Integer getWhitelistExpirySeconds() {
    return whitelistExpirySeconds;
  }

  @JsonProperty
  public void setWhitelistExpirySeconds(Integer whitelistExpirySeconds) {
    this.whitelistExpirySeconds = whitelistExpirySeconds;
  }

  public boolean isEnabled() {
    return StringUtils.isNotEmpty(secretKey);
  }
}
