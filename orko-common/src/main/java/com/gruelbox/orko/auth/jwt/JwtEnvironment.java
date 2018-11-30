package com.gruelbox.orko.auth.jwt;

import javax.inject.Inject;

import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.gruelbox.orko.OrkoConfiguration;
import com.gruelbox.orko.websocket.WebSocketModule;
import com.gruelbox.orko.wiring.EnvironmentInitialiser;

import io.dropwizard.setup.Environment;

@Singleton
public class JwtEnvironment implements EnvironmentInitialiser {

  private final OrkoConfiguration appConfiguration;
  private final Provider<JwtAuthenticationFilter> jwtAuthenticationFilter;
  private final Provider<JwtXsrfProtectionFilter> jwtXsrfProtectionFilter;

  @Inject
  JwtEnvironment(OrkoConfiguration appConfiguration,
                 Provider<JwtAuthenticationFilter> jwtAuthenticationFilter,
                 Provider<JwtXsrfProtectionFilter> jwtXsrfProtectionFilter) {
    this.appConfiguration = appConfiguration;
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    this.jwtXsrfProtectionFilter = jwtXsrfProtectionFilter;
  }

  @Override
  public void init(Environment environment) {

    if (appConfiguration.getAuth().getJwt() != null && appConfiguration.getAuth().getJwt().isEnabled()) {
      String rootPath = appConfiguration.getRootPath();
      String websocketEntryFilter = WebSocketModule.ENTRY_POINT + "/*";

      JwtXsrfProtectionFilter xsrfFilter = jwtXsrfProtectionFilter.get();
      environment.servlets().addFilter(JwtXsrfProtectionFilter.class.getSimpleName(), xsrfFilter)
        .addMappingForUrlPatterns(null, true, rootPath);

      JwtAuthenticationFilter authFilter = jwtAuthenticationFilter.get();
      environment.servlets().addFilter(JwtAuthenticationFilter.class.getSimpleName(), authFilter)
        .addMappingForUrlPatterns(null, true, rootPath, websocketEntryFilter);
      environment.admin().addFilter(JwtAuthenticationFilter.class.getSimpleName(), authFilter)
        .addMappingForUrlPatterns(null, true, "/*");

    }
  }
}