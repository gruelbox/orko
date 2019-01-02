package com.gruelbox.orko.app.monolith;

/*-
 * ===============================================================================L
 * Orko Monolith Application
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

import javax.inject.Inject;

import com.google.inject.Singleton;
import com.gruelbox.tools.dropwizard.guice.EnvironmentInitialiser;

import io.dropwizard.setup.Environment;

@Singleton
class MonolithEnvironment implements EnvironmentInitialiser {

  private final ClientSecurityHeadersFilter clientSecurityHeadersFilter;

  @Inject
  MonolithEnvironment(ClientSecurityHeadersFilter clientSecurityHeadersFilter) {
    this.clientSecurityHeadersFilter = clientSecurityHeadersFilter;
  }

  @Override
  public void init(Environment environment) {
    environment.servlets().addFilter(ClientSecurityHeadersFilter.class.getSimpleName(), clientSecurityHeadersFilter)
      .addMappingForUrlPatterns(null, true, "/*");
  }
}
