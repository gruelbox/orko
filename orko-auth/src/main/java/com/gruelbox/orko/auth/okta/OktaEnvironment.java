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

import javax.inject.Inject;

import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.gruelbox.orko.auth.AuthConfiguration;
import com.gruelbox.orko.auth.AuthModule;
import com.gruelbox.tools.dropwizard.guice.EnvironmentInitialiser;

import io.dropwizard.setup.Environment;

@Singleton
public class OktaEnvironment implements EnvironmentInitialiser {

  private final Provider<OktaAuthenticationFilter> oktaAuthenticationFilter;
  private final AuthConfiguration authConfiguration;
  private final String rootPath;
  private final String wsEntryPoint;

  @Inject
  OktaEnvironment(AuthConfiguration authConfiguration,
                  @Named(AuthModule.ROOT_PATH) String rootPath,
                  @Named(AuthModule.WEBSOCKET_ENTRY_POINT) String wsEntryPoint,
                  Provider<OktaAuthenticationFilter> oktaAuthenticationFilter) {
    this.rootPath = rootPath;
    this.wsEntryPoint = wsEntryPoint;
    this.oktaAuthenticationFilter = oktaAuthenticationFilter;
    this.authConfiguration = authConfiguration;
  }

  @Override
  public void init(Environment environment) {
    if (authConfiguration.getOkta() != null && authConfiguration.getOkta().isEnabled()) {
      String websocketEntryFilter = wsEntryPoint + "/*";

      OktaAuthenticationFilter authFilter = oktaAuthenticationFilter.get();
      environment.servlets().addFilter(OktaAuthenticationFilter.class.getSimpleName(), authFilter)
        .addMappingForUrlPatterns(null, true, rootPath, websocketEntryFilter);
      environment.admin().addFilter(OktaAuthenticationFilter.class.getSimpleName(), authFilter)
        .addMappingForUrlPatterns(null, true, "/*");
    }
  }
}
