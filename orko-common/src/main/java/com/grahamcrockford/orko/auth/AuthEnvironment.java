package com.grahamcrockford.orko.auth;

import javax.inject.Inject;

import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.grahamcrockford.orko.OrkoConfiguration;
import com.grahamcrockford.orko.auth.ipwhitelisting.IpWhitelistingEnvironment;
import com.grahamcrockford.orko.auth.jwt.JwtEnvironment;
import com.grahamcrockford.orko.auth.okta.OktaEnvironment;
import com.grahamcrockford.orko.websocket.WebSocketModule;
import com.grahamcrockford.orko.wiring.EnvironmentInitialiser;

import io.dropwizard.setup.Environment;

@Singleton
class AuthEnvironment implements EnvironmentInitialiser {

  private final IpWhitelistingEnvironment ipWhitelistingEnvironment;
  private final OktaEnvironment oktaEnvironment;
  private final JwtEnvironment jwtEnvironment;
  private final Provider<ProtocolToBearerTranslationFilter> protocolToBearerTranslationFilter;
  private final OrkoConfiguration appConfiguration;


  @Inject
  AuthEnvironment(IpWhitelistingEnvironment ipWhitelistingEnvironment,
                  OktaEnvironment oktaEnvironment,
                  JwtEnvironment jwtEnvironment,
                  Provider<ProtocolToBearerTranslationFilter> protocolToBearerTranslationFilter,
                  OrkoConfiguration appConfiguration) {
    this.ipWhitelistingEnvironment = ipWhitelistingEnvironment;
    this.oktaEnvironment = oktaEnvironment;
    this.jwtEnvironment = jwtEnvironment;
    this.protocolToBearerTranslationFilter = protocolToBearerTranslationFilter;
    this.appConfiguration = appConfiguration;
  }

  @Override
  public void init(Environment environment) {

    // Enable IP whitelisting
    ipWhitelistingEnvironment.init(environment);

    // Interceptor to convert protocol header into Bearer for use in websocket comms
    String rootPath = appConfiguration.getRootPath();
    String websocketEntryFilter = WebSocketModule.ENTRY_POINT + "/*";
    environment.servlets().addFilter(ProtocolToBearerTranslationFilter.class.getSimpleName(), protocolToBearerTranslationFilter.get())
      .addMappingForUrlPatterns(null, true, rootPath, websocketEntryFilter);

    // Finally Okta
    oktaEnvironment.init(environment);
    jwtEnvironment.init(environment);
  }
}