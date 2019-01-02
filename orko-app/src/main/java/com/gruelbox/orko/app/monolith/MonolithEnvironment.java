package com.gruelbox.orko.app.monolith;

import javax.inject.Inject;

import com.google.inject.Singleton;
import com.gruelbox.tools.dropwizard.guice.EnvironmentInitialiser;

import io.dropwizard.setup.Environment;

@Singleton
class MonolithEnvironment implements EnvironmentInitialiser {

  private final ClientSecurityHeadersFilter clientSecurityHeadersFilter;

  @Inject
  MonolithEnvironment(ClientSecurityHeadersFilter clientSecurityHeadersFilter) {
    this.clientSecurityHeadersFilter = clientSecurityHeadersFilter;
  }

  @Override
  public void init(Environment environment) {
    environment.servlets().addFilter(ClientSecurityHeadersFilter.class.getSimpleName(), clientSecurityHeadersFilter)
      .addMappingForUrlPatterns(null, true, "/*");
  }
}