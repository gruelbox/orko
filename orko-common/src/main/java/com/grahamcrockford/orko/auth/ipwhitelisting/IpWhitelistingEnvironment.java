package com.grahamcrockford.orko.auth.ipwhitelisting;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.google.inject.Singleton;
import com.grahamcrockford.orko.OrkoConfiguration;
import com.grahamcrockford.orko.auth.AuthConfiguration;
import com.grahamcrockford.orko.websocket.WebSocketModule;
import com.grahamcrockford.orko.wiring.EnvironmentInitialiser;

import io.dropwizard.server.AbstractServerFactory;
import io.dropwizard.setup.Environment;

@Singleton
public class IpWhitelistingEnvironment implements EnvironmentInitialiser {

  private final IpWhitelistServletFilter ipWhitelistServletFilter;
  private final AuthConfiguration authConfiguration;
  private final OrkoConfiguration appConfiguration;

  @Inject
  IpWhitelistingEnvironment(IpWhitelistServletFilter ipWhitelistServletFilter,
                  AuthConfiguration authConfiguration,
                  OrkoConfiguration appConfiguration) {
    this.ipWhitelistServletFilter = ipWhitelistServletFilter;
    this.authConfiguration = authConfiguration;
    this.appConfiguration = appConfiguration;
  }

  @Override
  public void init(Environment environment) {

    AbstractServerFactory serverFactory = (AbstractServerFactory) appConfiguration.getServerFactory();
    String rootPath = serverFactory.getJerseyRootPath().orElse("/") + "*";
    String websocketEntryFilter = WebSocketModule.ENTRY_POINT + "/*";

    // Apply IP whitelisting outside the authentication stack so we can provide a different response
    if (authConfiguration.getIpWhitelisting() != null && StringUtils.isNotEmpty(authConfiguration.getIpWhitelisting().getSecretKey())) {
      environment.servlets().addFilter(IpWhitelistServletFilter.class.getSimpleName(), ipWhitelistServletFilter)
        .addMappingForUrlPatterns(null, true, rootPath, websocketEntryFilter);
      environment.admin().addFilter(IpWhitelistServletFilter.class.getSimpleName(), ipWhitelistServletFilter)
        .addMappingForUrlPatterns(null, true, "/*");
    }
  }
}