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

package com.gruelbox.orko.db;


import com.google.inject.Provides;
import com.gruelbox.orko.wiring.AbstractConfiguredModule;

public class DbModule extends AbstractConfiguredModule<HasDbConfiguration> {

  @Override
  protected void configure() {
    install(new DatabaseAccessModule());
  }

  @Provides
  DbConfiguration dbConfiguration() {
    return getConfiguration().getDatabase();
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof DbModule;
  }

  @Override
  public int hashCode() {
    return DbModule.class.getName().hashCode();
  }
}
