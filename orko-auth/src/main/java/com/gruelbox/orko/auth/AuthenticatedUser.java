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

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import java.security.Principal;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

public class AuthenticatedUser implements Principal {

  private final String name;
  private final Set<String> roles;

  public AuthenticatedUser(String name, String roles) {
    this.name = name;
    this.roles =
        StringUtils.isEmpty(roles)
            ? ImmutableSet.of()
            : ImmutableSet.copyOf(Splitter.on(",").split(roles));
  }

  @Override
  public String getName() {
    return name;
  }

  public Set<String> getRoles() {
    return roles;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((roles == null) ? 0 : roles.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    AuthenticatedUser other = (AuthenticatedUser) obj;
    if (name == null) {
      if (other.name != null) return false;
    } else if (!name.equals(other.name)) return false;
    if (roles == null) {
      if (other.roles != null) return false;
    } else if (!roles.equals(other.roles)) return false;
    return true;
  }
}
