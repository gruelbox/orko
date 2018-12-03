package com.gruelbox.orko.auth.jwt;

import javax.inject.Inject;

import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.gruelbox.orko.auth.AuthConfiguration;
import com.gruelbox.orko.auth.AuthModule;
import com.gruelbox.tools.dropwizard.guice.EnvironmentInitialiser;

import io.dropwizard.setup.Environment;

@Singleton
public class JwtEnvironment implements EnvironmentInitialiser {

  private final AuthConfiguration configuration;
  private final Provider<JwtAuthenticationFilter> jwtAuthenticationFilter;
  private final Provider<JwtXsrfProtectionFilter> jwtXsrfProtectionFilter;
  private final String rootPath;
  private final String wsEntryPoint;

  @Inject
  JwtEnvironment(AuthConfiguration configuration,
                 @Named(AuthModule.ROOT_PATH) String rootPath,
                 @Named(AuthModule.WEBSOCKET_ENTRY_POINT) String wsEntryPoint,
                 Provider<JwtAuthenticationFilter> jwtAuthenticationFilter,
                 Provider<JwtXsrfProtectionFilter> jwtXsrfProtectionFilter) {
    this.configuration = configuration;
    this.rootPath = rootPath;
    this.wsEntryPoint = wsEntryPoint;
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    this.jwtXsrfProtectionFilter = jwtXsrfProtectionFilter;
  }

  @Override
  public void init(Environment environment) {

    if (configuration.getJwt() != null && configuration.getJwt().isEnabled()) {
      String websocketEntryFilter = wsEntryPoint + "/*";

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