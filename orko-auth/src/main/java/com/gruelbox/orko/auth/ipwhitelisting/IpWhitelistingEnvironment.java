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

import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.gruelbox.orko.auth.AuthConfiguration;
import com.gruelbox.orko.auth.AuthModule;
import com.gruelbox.tools.dropwizard.guice.EnvironmentInitialiser;
import io.dropwizard.setup.Environment;
import javax.inject.Inject;

@Singleton
public class IpWhitelistingEnvironment implements EnvironmentInitialiser {

  private final Provider<IpWhitelistServletFilter> ipWhitelistServletFilter;
  private final AuthConfiguration authConfiguration;
  private final String rootPath;
  private final String wsEntryPoint;

  @Inject
  IpWhitelistingEnvironment(
      Provider<IpWhitelistServletFilter> ipWhitelistServletFilter,
      AuthConfiguration authConfiguration,
      @Named(AuthModule.BIND_ROOT_PATH) String rootPath,
      @Named(AuthModule.BIND_WEBSOCKET_ENTRY_POINT) String wsEntryPoint) {
    this.ipWhitelistServletFilter = ipWhitelistServletFilter;
    this.authConfiguration = authConfiguration;
    this.rootPath = rootPath;
    this.wsEntryPoint = wsEntryPoint;
  }

  @Override
  public void init(Environment environment) {
    if (authConfiguration.getIpWhitelisting() != null
        && authConfiguration.getIpWhitelisting().isEnabled()) {
      environment
          .servlets()
          .addFilter(IpWhitelistServletFilter.class.getSimpleName(), ipWhitelistServletFilter.get())
          .addMappingForUrlPatterns(null, true, rootPath, wsEntryPoint + "/*");
      environment
          .admin()
          .addFilter(IpWhitelistServletFilter.class.getSimpleName(), ipWhitelistServletFilter.get())
          .addMappingForUrlPatterns(null, true, "/*");
    }
  }
}
