package com.gruelbox.orko.auth;

import java.util.Optional;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import com.google.inject.servlet.RequestScoped;
import com.gruelbox.orko.OrkoConfiguration;
import com.gruelbox.orko.auth.blacklist.Blacklisting;
import com.gruelbox.orko.auth.ipwhitelisting.IpWhitelistingModule;
import com.gruelbox.orko.auth.jwt.JwtModule;
import com.gruelbox.orko.auth.okta.OktaModule;
import com.gruelbox.orko.wiring.EnvironmentInitialiser;

import io.dropwizard.lifecycle.Managed;

public class AuthModule extends AbstractModule {

  private final OrkoConfiguration configuration;

  public static final String ACCESS_TOKEN_KEY = "accessToken";

  public AuthModule(OrkoConfiguration configuration) {
    this.configuration = configuration;
  }

  @Override
  protected void configure() {
    if (configuration.getAuth() != null) {
      install(new GoogleAuthenticatorModule());
      install(new IpWhitelistingModule());
      install(new OktaModule(configuration.getAuth()));
      install(new JwtModule(configuration.getAuth()));
      Multibinder.newSetBinder(binder(), EnvironmentInitialiser.class).addBinding().to(AuthEnvironment.class);
      Multibinder.newSetBinder(binder(), Managed.class).addBinding().to(Blacklisting.class);
      install(new Testing());
    }
  }

  public static final class Testing extends AbstractModule {

    @Override
    protected void configure() {
      bind(new TypeLiteral<Optional<String>>() {}).annotatedWith(Names.named(ACCESS_TOKEN_KEY))
        .toProvider(AccessTokenProvider.class)
        .in(RequestScoped.class);
    }

    @Provides
    AuthConfiguration authConfiguration(OrkoConfiguration orkoConfiguration) {
      return orkoConfiguration.getAuth();
    }

  }
}