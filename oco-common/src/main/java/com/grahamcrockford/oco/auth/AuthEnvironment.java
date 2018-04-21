package com.grahamcrockford.oco.auth;

import javax.inject.Inject;
import com.google.inject.Singleton;
import com.grahamcrockford.oco.wiring.EnvironmentInitialiser;
import io.dropwizard.setup.Environment;

@Singleton
class AuthEnvironment implements EnvironmentInitialiser {

  private final IpWhitelistServletFilter ipWhitelistServletFilter;
  private final BearerAuthenticationFilter bearerAuthenticationFilter;
  private final ProtocolToBearerTranslationFilter protocolToBearerTranslationFilter;

  @Inject
  AuthEnvironment(IpWhitelistServletFilter ipWhitelistServletFilter,
                  BearerAuthenticationFilter bearerAuthenticationFilter,
                  ProtocolToBearerTranslationFilter protocolToBearerTranslationFilter) {
    this.ipWhitelistServletFilter = ipWhitelistServletFilter;
    this.bearerAuthenticationFilter = bearerAuthenticationFilter;
    this.protocolToBearerTranslationFilter = protocolToBearerTranslationFilter;
  }

  @Override
  public void init(Environment environment) {

    // Apply IP whitelisting outside the authentication stack so we can provide a different response
    environment.servlets().addFilter(IpWhitelistServletFilter.class.getSimpleName(), ipWhitelistServletFilter)
      .addMappingForUrlPatterns(null, true, "/*");

    // Interceptor to convert protocol header into Bearer for use in websocket comms
    environment.servlets().addFilter(ProtocolToBearerTranslationFilter.class.getSimpleName(), protocolToBearerTranslationFilter)
      .addMappingForUrlPatterns(null, true, "/*");

    // And finally validate the JWT
    environment.servlets().addFilter(BearerAuthenticationFilter.class.getSimpleName(), bearerAuthenticationFilter)
      .addMappingForUrlPatterns(null, true, "/*");

    // And the same for the admin servlet
    environment.admin().addFilter(IpWhitelistServletFilter.class.getSimpleName(), ipWhitelistServletFilter)
      .addMappingForUrlPatterns(null, true, "/*");
    environment.admin().addFilter(BearerAuthenticationFilter.class.getSimpleName(), bearerAuthenticationFilter)
      .addMappingForUrlPatterns(null, true, "/*");
  }
}