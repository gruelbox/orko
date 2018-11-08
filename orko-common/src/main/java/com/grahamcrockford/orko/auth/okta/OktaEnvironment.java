package com.grahamcrockford.orko.auth.okta;

import javax.inject.Inject;

import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.grahamcrockford.orko.OrkoConfiguration;
import com.grahamcrockford.orko.auth.AuthConfiguration;
import com.grahamcrockford.orko.auth.okta.BearerAuthenticationFilter;
import com.grahamcrockford.orko.websocket.WebSocketModule;
import com.grahamcrockford.orko.wiring.EnvironmentInitialiser;

import io.dropwizard.setup.Environment;

@Singleton
public class OktaEnvironment implements EnvironmentInitialiser {

  private final Provider<BearerAuthenticationFilter> bearerAuthenticationFilter;
  private final AuthConfiguration authConfiguration;
  private final OrkoConfiguration appConfiguration;

  @Inject
  OktaEnvironment(AuthConfiguration authConfiguration,
                  OrkoConfiguration appConfiguration,
                  Provider<BearerAuthenticationFilter> bearerAuthenticationFilter) {
    this.bearerAuthenticationFilter = bearerAuthenticationFilter;
    this.authConfiguration = authConfiguration;
    this.appConfiguration = appConfiguration;
  }

  @Override
  public void init(Environment environment) {
    if (authConfiguration.getOkta() != null && authConfiguration.getOkta().isEnabled()) {
      String rootPath = appConfiguration.getRootPath();
      String websocketEntryFilter = WebSocketModule.ENTRY_POINT + "/*";
      environment.servlets().addFilter(BearerAuthenticationFilter.class.getSimpleName(), bearerAuthenticationFilter.get())
        .addMappingForUrlPatterns(null, true, rootPath, websocketEntryFilter);
      environment.admin().addFilter(BearerAuthenticationFilter.class.getSimpleName(), bearerAuthenticationFilter.get())
        .addMappingForUrlPatterns(null, true, "/*");
    }
  }
}