package com.grahamcrockford.oco.auth;

import java.util.EnumSet;

import javax.inject.Inject;
import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;

import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;

import com.google.inject.Singleton;
import com.grahamcrockford.oco.EnvironmentInitialiser;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.setup.Environment;

@Singleton
class AuthEnvironment implements EnvironmentInitialiser {

  private final SimpleAuthenticator authenticator;
  private final AuthConfiguration configuration;

  @Inject
  AuthEnvironment(SimpleAuthenticator authenticator, AuthConfiguration configuration) {
    this.authenticator = authenticator;
    this.configuration = configuration;
  }

  @Override
  public void init(Environment environment) {

    // Allow CORS
    // TODO needs to be more secure.
    if (configuration.isCors()) {
      final FilterRegistration.Dynamic cors =
          environment.servlets().addFilter("CORS", CrossOriginFilter.class);
      cors.setInitParameter("allowedOrigins", "*");
      cors.setInitParameter("allowedHeaders", "X-Requested-With,Content-Type,Accept,Origin,Authorization");
      cors.setInitParameter("allowedMethods", "OPTIONS,GET,PUT,POST,DELETE,HEAD");
      cors.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");
    }

    // Auth
    environment.jersey().register(new AuthDynamicFeature(new BasicCredentialAuthFilter.Builder<User>()
      .setAuthenticator(authenticator)
      .setAuthorizer(authenticator)
      .setRealm("SUPER SECRET STUFF")
      .buildAuthFilter()
    ));
    environment.jersey().register(RolesAllowedDynamicFeature.class);

  }
}