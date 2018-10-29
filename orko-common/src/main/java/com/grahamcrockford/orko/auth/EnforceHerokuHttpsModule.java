package com.grahamcrockford.orko.auth;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.multibindings.Multibinder;
import com.grahamcrockford.orko.wiring.EnvironmentInitialiser;

import io.dropwizard.setup.Environment;

public class EnforceHerokuHttpsModule extends AbstractModule {

  private static final Logger LOGGER = LoggerFactory.getLogger(EnforceHerokuHttpsModule.class);

  @Override
  protected void configure() {
    Multibinder.newSetBinder(binder(), EnvironmentInitialiser.class).addBinding().to(EnforceHttpsEnvironment.class);
  }

  private static final class EnforceHttpsEnvironment implements EnvironmentInitialiser {

    private final AuthConfiguration configuration;

    @Inject
    EnforceHttpsEnvironment(AuthConfiguration configuration) {
      this.configuration = configuration;
    }

    @Override
    public void init(Environment environment) {
      if (configuration.isHttpsOnly()) {
        LOGGER.info("Restricting to HTTPS only on Heroku.");
        FilterRegistration.Dynamic httpsEnforcer = environment.servlets()
            .addFilter("HttpsEnforcer", new HttpsEnforcer());
        httpsEnforcer.addMappingForUrlPatterns(null, true, "/*");
      }
    }
  }

  private static final class HttpsEnforcer implements Filter {

    public static final String X_FORWARDED_PROTO = "X-Forwarded-Proto";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
      // No-op
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
      HttpServletRequest request = (HttpServletRequest) servletRequest;
      HttpServletResponse response = (HttpServletResponse) servletResponse;

      if (request.getHeader(X_FORWARDED_PROTO) != null && request.getHeader(X_FORWARDED_PROTO).indexOf("https") != 0) {
        String pathInfo = (request.getPathInfo() != null) ? request.getPathInfo() : "";
        String redirect = "https://" + request.getServerName() + pathInfo;
        LOGGER.error("Unsecured access redirected to [{}]", redirect);
        response.sendRedirect(redirect);
        return;
      }

      filterChain.doFilter(request, response);
    }

    @Override
    public void destroy() {
      // No-op
    }
  }
}
