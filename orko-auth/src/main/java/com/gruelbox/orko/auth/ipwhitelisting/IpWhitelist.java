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

package com.gruelbox.orko.auth.ipwhitelisting;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Persistence object for IP whitelisting.
 *
 * @author Graham Crockford
 */
@Entity(name = IpWhitelist.TABLE_NAME)
final class IpWhitelist {

  static final String IP = "ip";
  static final String EXPIRES = "expires";
  static final String TABLE_NAME = "IpWhitelist";

  @Id
  @Column(name = IP, nullable = false)
  @NotNull
  @JsonProperty
  private String ip;

  @Column(name = EXPIRES, nullable = false)
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
