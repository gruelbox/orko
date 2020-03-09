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

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import com.google.inject.servlet.RequestScoped;
import com.gruelbox.orko.auth.blacklist.Blacklisting;
import com.gruelbox.orko.auth.ipwhitelisting.IpWhitelistingModule;
import com.gruelbox.orko.auth.jwt.JwtModule;
import com.gruelbox.orko.db.DbModule;
import com.gruelbox.tools.dropwizard.guice.EnvironmentInitialiser;
import io.dropwizard.lifecycle.Managed;
import java.util.Optional;

public class AuthModule extends AbstractModule {

  public static final String BIND_ACCESS_TOKEN_KEY = "accessToken";
  public static final String BIND_ROOT_PATH = "auth-rootPath";
  public static final String BIND_WEBSOCKET_ENTRY_POINT = "auth-ws-entry";

  private AuthConfiguration configuration;

  public AuthModule(AuthConfiguration configuration) {
    this.configuration = configuration;
  }

  @Override
  protected void configure() {
    install(new DbModule());
    if (configuration != null) {
      install(new GoogleAuthenticatorModule());
      install(new IpWhitelistingModule());
      install(new JwtModule(configuration));
      Multibinder.newSetBinder(binder(), EnvironmentInitialiser.class)
          .addBinding()
          .to(AuthEnvironment.class);
      Multibinder.newSetBinder(binder(), Managed.class).addBinding().to(Blacklisting.class);
      install(new Testing());
    }
  }

  public static final class Testing extends AbstractModule {
    @Override
    protected void configure() {
      bind(new TypeLiteral<Optional<String>>() {})
          .annotatedWith(Names.named(BIND_ACCESS_TOKEN_KEY))
          .toProvider(AccessTokenProvider.class)
          .in(RequestScoped.class);
    }
  }
}
