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
package com.gruelbox.orko.job;

import com.google.auto.value.AutoValue;

@AutoValue
abstract class LogColumn {

  static Builder builder() {
    return new AutoValue_LogColumn.Builder();
  }

  @AutoValue.Builder
  abstract static class Builder {
    public abstract Builder name(String name);

    public abstract Builder width(int width);

    public abstract Builder rightAligned(boolean rightAligned);

    public abstract LogColumn build();
  }

  public abstract String name();

  public abstract int width();

  public abstract boolean rightAligned();
}
