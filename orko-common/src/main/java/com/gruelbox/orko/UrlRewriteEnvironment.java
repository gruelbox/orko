package com.gruelbox.orko;

import javax.servlet.FilterRegistration;

import org.tuckey.web.filters.urlrewrite.UrlRewriteFilter;

import com.gruelbox.tools.dropwizard.guice.EnvironmentInitialiser;

import io.dropwizard.setup.Environment;

class UrlRewriteEnvironment implements EnvironmentInitialiser {
  @Override
  public void init(Environment environment) {
    FilterRegistration.Dynamic urlRewriteFilter = environment.servlets()
        .addFilter("UrlRewriteFilter", new UrlRewriteFilter());
    urlRewriteFilter.addMappingForUrlPatterns(null, true, "/*");
    urlRewriteFilter.setInitParameter("confPath", "urlrewrite.xml");
  }
}