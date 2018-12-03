package com.gruelbox.orko.auth;

import java.util.Optional;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import com.google.inject.servlet.RequestScoped;
import com.gruelbox.orko.auth.blacklist.Blacklisting;
import com.gruelbox.orko.auth.ipwhitelisting.IpWhitelistingModule;
import com.gruelbox.orko.auth.jwt.JwtModule;
import com.gruelbox.orko.auth.okta.OktaModule;
import com.gruelbox.tools.dropwizard.guice.EnvironmentInitialiser;

import io.dropwizard.lifecycle.Managed;

public class AuthModule extends AbstractModule {

  public static final String ACCESS_TOKEN_KEY = "accessToken";
  public static final String ROOT_PATH = "auth-rootPath";
  public static final String WEBSOCKET_ENTRY_POINT = "auth-ws-entry";

  private final AuthConfiguration configuration;

  public AuthModule(AuthConfiguration configuration) {
    this.configuration = configuration;
  }

  @Override
  protected void configure() {
    if (configuration != null) {
      install(new GoogleAuthenticatorModule());
      install(new IpWhitelistingModule());
      install(new OktaModule(configuration));
      install(new JwtModule(configuration));
      Multibinder.newSetBinder(binder(), EnvironmentInitialiser.class).addBinding().to(AuthEnvironment.class);
      Multibinder.newSetBinder(binder(), Managed.class).addBinding().to(Blacklisting.class);
      install(new Testing(configuration));
    }
  }

  public static final class Testing extends AbstractModule {

    private final AuthConfiguration configuration;

    public Testing(AuthConfiguration configuration) {
      this.configuration = configuration;
    }

    @Override
    protected void configure() {
      bind(AuthConfiguration.class).toInstance(configuration);
      bind(new TypeLiteral<Optional<String>>() {}).annotatedWith(Names.named(ACCESS_TOKEN_KEY))
        .toProvider(AccessTokenProvider.class)
        .in(RequestScoped.class);
    }
  }
}