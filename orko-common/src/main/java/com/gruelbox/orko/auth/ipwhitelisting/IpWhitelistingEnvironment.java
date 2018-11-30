package com.gruelbox.orko.auth.ipwhitelisting;

import javax.inject.Inject;

import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.gruelbox.orko.OrkoConfiguration;
import com.gruelbox.orko.auth.AuthConfiguration;
import com.gruelbox.orko.websocket.WebSocketModule;
import com.gruelbox.orko.wiring.EnvironmentInitialiser;

import io.dropwizard.setup.Environment;

@Singleton
public class IpWhitelistingEnvironment implements EnvironmentInitialiser {

  private final Provider<IpWhitelistServletFilter> ipWhitelistServletFilter;
  private final AuthConfiguration authConfiguration;
  private final OrkoConfiguration appConfiguration;

  @Inject
  IpWhitelistingEnvironment(Provider<IpWhitelistServletFilter> ipWhitelistServletFilter,
                            AuthConfiguration authConfiguration,
                            OrkoConfiguration appConfiguration) {
    this.ipWhitelistServletFilter = ipWhitelistServletFilter;
    this.authConfiguration = authConfiguration;
    this.appConfiguration = appConfiguration;
  }

  @Override
  public void init(Environment environment) {
    if (authConfiguration.getIpWhitelisting() != null && authConfiguration.getIpWhitelisting().isEnabled()) {
      environment.servlets().addFilter(IpWhitelistServletFilter.class.getSimpleName(), ipWhitelistServletFilter.get())
        .addMappingForUrlPatterns(null, true, appConfiguration.getRootPath(), WebSocketModule.ENTRY_POINT + "/*");
      environment.admin().addFilter(IpWhitelistServletFilter.class.getSimpleName(), ipWhitelistServletFilter.get())
        .addMappingForUrlPatterns(null, true, "/*");
    }
  }
}