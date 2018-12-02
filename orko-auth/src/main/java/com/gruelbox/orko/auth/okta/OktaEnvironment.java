package com.gruelbox.orko.auth.okta;

import javax.inject.Inject;

import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.gruelbox.orko.auth.AuthConfiguration;
import com.gruelbox.orko.auth.AuthModule;
import com.gruelbox.orko.wiring.EnvironmentInitialiser;

import io.dropwizard.setup.Environment;

@Singleton
public class OktaEnvironment implements EnvironmentInitialiser {

  private final Provider<OktaAuthenticationFilter> oktaAuthenticationFilter;
  private final AuthConfiguration authConfiguration;
  private final String rootPath;
  private final String wsEntryPoint;

  @Inject
  OktaEnvironment(AuthConfiguration authConfiguration,
                  @Named(AuthModule.ROOT_PATH) String rootPath,
                  @Named(AuthModule.WEBSOCKET_ENTRY_POINT) String wsEntryPoint,
                  Provider<OktaAuthenticationFilter> oktaAuthenticationFilter) {
    this.rootPath = rootPath;
    this.wsEntryPoint = wsEntryPoint;
    this.oktaAuthenticationFilter = oktaAuthenticationFilter;
    this.authConfiguration = authConfiguration;
  }

  @Override
  public void init(Environment environment) {
    if (authConfiguration.getOkta() != null && authConfiguration.getOkta().isEnabled()) {
      String websocketEntryFilter = wsEntryPoint + "/*";

      OktaAuthenticationFilter authFilter = oktaAuthenticationFilter.get();
      environment.servlets().addFilter(OktaAuthenticationFilter.class.getSimpleName(), authFilter)
        .addMappingForUrlPatterns(null, true, rootPath, websocketEntryFilter);
      environment.admin().addFilter(OktaAuthenticationFilter.class.getSimpleName(), authFilter)
        .addMappingForUrlPatterns(null, true, "/*");
    }
  }
}