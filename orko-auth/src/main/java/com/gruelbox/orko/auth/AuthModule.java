package com.gruelbox.orko.auth;

import static com.google.inject.name.Names.named;

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
import com.gruelbox.orko.wiring.EnvironmentInitialiser;

import io.dropwizard.lifecycle.Managed;

public class AuthModule extends AbstractModule {

  private final AuthConfiguration configuration;
  private final String rootPath;
  private final String webSocketEntryPoint;

  public static final String ACCESS_TOKEN_KEY = "accessToken";
  public static final String ROOT_PATH = "auth-rootPath";
  public static final String WEBSOCKET_ENTRY_POINT = "auth-ws-entry";

  public AuthModule(AuthConfiguration configuration, String rootPath, String webSocketEntryPoint) {
    this.configuration = configuration;
    this.rootPath = rootPath;
    this.webSocketEntryPoint = webSocketEntryPoint;
  }

  @Override
  protected void configure() {
    bind(String.class).annotatedWith(named(ROOT_PATH)).toInstance(rootPath);
    bind(String.class).annotatedWith(named(WEBSOCKET_ENTRY_POINT)).toInstance(webSocketEntryPoint);
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