package com.grahamcrockford.orko.allinone;

import javax.inject.Inject;

import com.google.inject.Singleton;
import com.grahamcrockford.orko.wiring.EnvironmentInitialiser;

import io.dropwizard.setup.Environment;

@Singleton
class AllInOneEnvironment implements EnvironmentInitialiser {

  private final ClientSecurityHeadersFilter clientSecurityHeadersFilter;

  @Inject
  AllInOneEnvironment(ClientSecurityHeadersFilter clientSecurityHeadersFilter) {
    this.clientSecurityHeadersFilter = clientSecurityHeadersFilter;
  }

  @Override
  public void init(Environment environment) {
    environment.servlets().addFilter(ClientSecurityHeadersFilter.class.getSimpleName(), clientSecurityHeadersFilter)
      .addMappingForUrlPatterns(null, true, "/", "/index.html");
  }
}