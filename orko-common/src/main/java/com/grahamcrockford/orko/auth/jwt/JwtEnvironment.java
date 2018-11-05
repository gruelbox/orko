package com.grahamcrockford.orko.auth.jwt;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.grahamcrockford.orko.OrkoConfiguration;
import com.grahamcrockford.orko.websocket.WebSocketModule;
import com.grahamcrockford.orko.wiring.EnvironmentInitialiser;

import io.dropwizard.setup.Environment;

@Singleton
public class JwtEnvironment implements EnvironmentInitialiser {

  private final OrkoConfiguration appConfiguration;
  private final Provider<JwtAuthenticationFilter> jwtAuthenticationFilter;

  @Inject
  JwtEnvironment(OrkoConfiguration appConfiguration, Provider<JwtAuthenticationFilter> jwtAuthenticationFilter) {
    this.appConfiguration = appConfiguration;
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
  }

  @Override
  public void init(Environment environment) {
    if (appConfiguration.getAuth().getJwt() != null && StringUtils.isNotEmpty(appConfiguration.getAuth().getJwt().getSecret())) {
      String rootPath = appConfiguration.getRootPath();
      String websocketEntryFilter = WebSocketModule.ENTRY_POINT + "/*";
      environment.servlets().addFilter(JwtAuthenticationFilter.class.getSimpleName(), jwtAuthenticationFilter.get())
        .addMappingForUrlPatterns(null, true, rootPath, websocketEntryFilter);
      environment.admin().addFilter(JwtAuthenticationFilter.class.getSimpleName(), jwtAuthenticationFilter.get())
        .addMappingForUrlPatterns(null, true, "/*");
    }
  }
}