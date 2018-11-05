package com.grahamcrockford.orko.auth;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.grahamcrockford.orko.OrkoConfiguration;
import com.grahamcrockford.orko.auth.ipwhitelisting.IpWhitelistingEnvironment;
import com.grahamcrockford.orko.websocket.WebSocketModule;
import com.grahamcrockford.orko.wiring.EnvironmentInitialiser;

import io.dropwizard.server.AbstractServerFactory;
import io.dropwizard.setup.Environment;

@Singleton
class AuthEnvironment implements EnvironmentInitialiser {

  private final IpWhitelistingEnvironment ipWhitelistingEnvironment;
  private final Provider<BearerAuthenticationFilter> bearerAuthenticationFilter;
  private final  Provider<ProtocolToBearerTranslationFilter> protocolToBearerTranslationFilter;
  private final AuthConfiguration authConfiguration;
  private final OrkoConfiguration appConfiguration;

  @Inject
  AuthEnvironment(IpWhitelistingEnvironment ipWhitelistingEnvironment,
                  Provider<BearerAuthenticationFilter> bearerAuthenticationFilter,
                  Provider<ProtocolToBearerTranslationFilter> protocolToBearerTranslationFilter,
                  AuthConfiguration authConfiguration,
                  OrkoConfiguration appConfiguration) {
    this.ipWhitelistingEnvironment = ipWhitelistingEnvironment;
    this.bearerAuthenticationFilter = bearerAuthenticationFilter;
    this.protocolToBearerTranslationFilter = protocolToBearerTranslationFilter;
    this.authConfiguration = authConfiguration;
    this.appConfiguration = appConfiguration;
  }

  @Override
  public void init(Environment environment) {
    ipWhitelistingEnvironment.init(environment);

    AbstractServerFactory serverFactory = (AbstractServerFactory) appConfiguration.getServerFactory();
    String rootPath = serverFactory.getJerseyRootPath().orElse("/") + "*";
    String websocketEntryFilter = WebSocketModule.ENTRY_POINT + "/*";

    // Interceptor to convert protocol header into Bearer for use in websocket comms
    // And finally validate the JWT
    if (authConfiguration.getOkta() != null && StringUtils.isNotEmpty(authConfiguration.getOkta().getIssuer())) {
      environment.servlets().addFilter(ProtocolToBearerTranslationFilter.class.getSimpleName(), protocolToBearerTranslationFilter.get())
        .addMappingForUrlPatterns(null, true, rootPath, websocketEntryFilter);
      environment.servlets().addFilter(BearerAuthenticationFilter.class.getSimpleName(), bearerAuthenticationFilter.get())
        .addMappingForUrlPatterns(null, true, rootPath, websocketEntryFilter);
      environment.admin().addFilter(BearerAuthenticationFilter.class.getSimpleName(), bearerAuthenticationFilter.get())
        .addMappingForUrlPatterns(null, true, rootPath, websocketEntryFilter);
    }
  }
}