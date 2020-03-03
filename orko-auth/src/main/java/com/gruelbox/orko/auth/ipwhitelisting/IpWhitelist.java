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
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

/**
 * Persistence object for IP whitelisting.
 *
 * @author Graham Crockford
 */
@Entity(name = IpWhitelist.TABLE_NAME)
final class IpWhitelist {

  static final String IP_FIELD = "ip";
  static final String EXPIRES_FIELD = "expires";
  static final String TABLE_NAME = "IpWhitelist";

  @Id
  @Column(name = IP_FIELD, nullable = false)
  @NotNull
  @JsonProperty
  private String ip;

  @Column(name = EXPIRES_FIELD, nullable = false)
  @NotNull
  @JsonProperty
  private long expires;

  IpWhitelist() {
    // Nothing to do
  }

  IpWhitelist(String ip, long expires) {
    super();
    this.ip = ip;
    this.expires = expires;
  }
}
