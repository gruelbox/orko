package com.gruelbox.orko.auth.okta;

import javax.inject.Inject;

import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.gruelbox.orko.OrkoConfiguration;
import com.gruelbox.orko.auth.AuthConfiguration;
import com.gruelbox.orko.websocket.WebSocketModule;
import com.gruelbox.orko.wiring.EnvironmentInitialiser;

import io.dropwizard.setup.Environment;

@Singleton
public class OktaEnvironment implements EnvironmentInitialiser {

  private final Provider<OktaAuthenticationFilter> oktaAuthenticationFilter;
  private final AuthConfiguration authConfiguration;
  private final OrkoConfiguration appConfiguration;

  @Inject
  OktaEnvironment(AuthConfiguration authConfiguration,
                  OrkoConfiguration appConfiguration,
                  Provider<OktaAuthenticationFilter> oktaAuthenticationFilter) {
    this.oktaAuthenticationFilter = oktaAuthenticationFilter;
    this.authConfiguration = authConfiguration;
    this.appConfiguration = appConfiguration;
  }

  @Override
  public void init(Environment environment) {
    if (authConfiguration.getOkta() != null && authConfiguration.getOkta().isEnabled()) {
      String rootPath = appConfiguration.getRootPath();
      String websocketEntryFilter = WebSocketModule.ENTRY_POINT + "/*";

      OktaAuthenticationFilter authFilter = oktaAuthenticationFilter.get();
      environment.servlets().addFilter(OktaAuthenticationFilter.class.getSimpleName(), authFilter)
        .addMappingForUrlPatterns(null, true, rootPath, websocketEntryFilter);
      environment.admin().addFilter(OktaAuthenticationFilter.class.getSimpleName(), authFilter)
        .addMappingForUrlPatterns(null, true, "/*");
    }
  }
}