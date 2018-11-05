package com.grahamcrockford.orko.auth;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.grahamcrockford.orko.OrkoConfiguration;
import com.grahamcrockford.orko.auth.ipwhitelisting.IpWhitelistingEnvironment;
import com.grahamcrockford.orko.auth.okta.OktaEnvironment;
import com.grahamcrockford.orko.websocket.WebSocketModule;
import com.grahamcrockford.orko.wiring.EnvironmentInitialiser;

import io.dropwizard.server.AbstractServerFactory;
import io.dropwizard.setup.Environment;

@Singleton
class AuthEnvironment implements EnvironmentInitialiser {

  private final IpWhitelistingEnvironment ipWhitelistingEnvironment;
  private final OktaEnvironment oktaEnvironment;
  private final Provider<ProtocolToBearerTranslationFilter> protocolToBearerTranslationFilter;
  private final AuthConfiguration authConfiguration;
  private final OrkoConfiguration appConfiguration;


  @Inject
  AuthEnvironment(IpWhitelistingEnvironment ipWhitelistingEnvironment,
                  OktaEnvironment oktaEnvironment,
                  Provider<ProtocolToBearerTranslationFilter> protocolToBearerTranslationFilter,
                  AuthConfiguration authConfiguration,
                  OrkoConfiguration appConfiguration) {
    this.ipWhitelistingEnvironment = ipWhitelistingEnvironment;
    this.oktaEnvironment = oktaEnvironment;
    this.protocolToBearerTranslationFilter = protocolToBearerTranslationFilter;
    this.authConfiguration = authConfiguration;
    this.appConfiguration = appConfiguration;
  }

  @Override
  public void init(Environment environment) {

    // Enable IP whitelisting
    ipWhitelistingEnvironment.init(environment);

    // Interceptor to convert protocol header into Bearer for use in websocket comms
    AbstractServerFactory serverFactory = (AbstractServerFactory) appConfiguration.getServerFactory();
    String rootPath = serverFactory.getJerseyRootPath().orElse("/") + "*";
    String websocketEntryFilter = WebSocketModule.ENTRY_POINT + "/*";
    if (authConfiguration.getOkta() != null && StringUtils.isNotEmpty(authConfiguration.getOkta().getIssuer())) {
      environment.servlets().addFilter(ProtocolToBearerTranslationFilter.class.getSimpleName(), protocolToBearerTranslationFilter.get())
        .addMappingForUrlPatterns(null, true, rootPath, websocketEntryFilter);
    }

    // Finally Okta
    oktaEnvironment.init(environment);
  }
}