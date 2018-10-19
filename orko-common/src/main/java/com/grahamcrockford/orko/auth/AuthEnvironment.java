package com.grahamcrockford.orko.auth;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.grahamcrockford.orko.OrkoConfiguration;
import com.grahamcrockford.orko.wiring.EnvironmentInitialiser;

import io.dropwizard.server.AbstractServerFactory;
import io.dropwizard.setup.Environment;

@Singleton
class AuthEnvironment implements EnvironmentInitialiser {

  private final IpWhitelistServletFilter ipWhitelistServletFilter;
  private final Provider<BearerAuthenticationFilter> bearerAuthenticationFilter;
  private final  Provider<ProtocolToBearerTranslationFilter> protocolToBearerTranslationFilter;
  private final AuthConfiguration authConfiguration;
  private final OrkoConfiguration appConfiguration;

  @Inject
  AuthEnvironment(IpWhitelistServletFilter ipWhitelistServletFilter,
                  Provider<BearerAuthenticationFilter> bearerAuthenticationFilter,
                  Provider<ProtocolToBearerTranslationFilter> protocolToBearerTranslationFilter,
                  AuthConfiguration authConfiguration,
                  OrkoConfiguration appConfiguration) {
    this.ipWhitelistServletFilter = ipWhitelistServletFilter;
    this.bearerAuthenticationFilter = bearerAuthenticationFilter;
    this.protocolToBearerTranslationFilter = protocolToBearerTranslationFilter;
    this.authConfiguration = authConfiguration;
    this.appConfiguration = appConfiguration;
  }

  @Override
  public void init(Environment environment) {

    AbstractServerFactory serverFactory = (AbstractServerFactory) appConfiguration.getServerFactory();
    String rootPath = serverFactory.getJerseyRootPath().orElse("/") + "*";

    // Apply IP whitelisting outside the authentication stack so we can provide a different response
    if (StringUtils.isNotEmpty(authConfiguration.secretKey)) {
      environment.servlets().addFilter(IpWhitelistServletFilter.class.getSimpleName(), ipWhitelistServletFilter)
        .addMappingForUrlPatterns(null, true, rootPath, "/ws/*");
      environment.admin().addFilter(IpWhitelistServletFilter.class.getSimpleName(), ipWhitelistServletFilter)
        .addMappingForUrlPatterns(null, true, rootPath, "/ws/*");
    }

    // Interceptor to convert protocol header into Bearer for use in websocket comms
    // And finally validate the JWT
    if (authConfiguration.okta != null && StringUtils.isNotEmpty(authConfiguration.okta.issuer)) {
      environment.servlets().addFilter(ProtocolToBearerTranslationFilter.class.getSimpleName(), protocolToBearerTranslationFilter.get())
        .addMappingForUrlPatterns(null, true, rootPath, "/ws/*");
      environment.servlets().addFilter(BearerAuthenticationFilter.class.getSimpleName(), bearerAuthenticationFilter.get())
        .addMappingForUrlPatterns(null, true, rootPath, "/ws/*");
      environment.admin().addFilter(BearerAuthenticationFilter.class.getSimpleName(), bearerAuthenticationFilter.get())
        .addMappingForUrlPatterns(null, true, rootPath, "/ws/*");
    }
  }
}