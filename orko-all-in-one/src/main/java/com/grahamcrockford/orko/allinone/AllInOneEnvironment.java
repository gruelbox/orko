package com.grahamcrockford.orko.allinone;

import javax.inject.Inject;

import com.google.inject.Singleton;
import com.grahamcrockford.orko.OrkoConfiguration;
import com.grahamcrockford.orko.wiring.EnvironmentInitialiser;

import io.dropwizard.setup.Environment;

@Singleton
class AllInOneEnvironment implements EnvironmentInitialiser {

  private final ClientSecurityHeadersFilter clientSecurityHeadersFilter;
  private final OrkoConfiguration orkoConfiguration;

  @Inject
  AllInOneEnvironment(ClientSecurityHeadersFilter clientSecurityHeadersFilter, OrkoConfiguration orkoConfiguration) {
    this.clientSecurityHeadersFilter = clientSecurityHeadersFilter;
    this.orkoConfiguration = orkoConfiguration;
  }

  @Override
  public void init(Environment environment) {
    environment.servlets().addFilter(ClientSecurityHeadersFilter.class.getSimpleName(), clientSecurityHeadersFilter)
      .addMappingForUrlPatterns(null, true, orkoConfiguration.getRootPath());
    environment.admin().addFilter(ClientSecurityHeadersFilter.class.getSimpleName(), clientSecurityHeadersFilter)
      .addMappingForUrlPatterns(null, true, "/*");
  }
}