package com.grahamcrockford.orko.auth;

import static com.grahamcrockford.orko.auth.Headers.X_FORWARDED_PROTO;

import java.io.IOException;
import java.util.regex.Pattern;

import javax.servlet.FilterChain;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.util.URIUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.multibindings.Multibinder;
import com.grahamcrockford.orko.wiring.EnvironmentInitialiser;

import io.dropwizard.setup.Environment;

class HttpsEnforcementModule extends AbstractModule {

  private static final Logger LOGGER = LoggerFactory.getLogger(HttpsEnforcementModule.class);

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
        LOGGER.info("Restricting to HTTPS only.");
        FilterRegistration.Dynamic httpsEnforcer = environment.servlets().addFilter(HttpsEnforcer.class.getSimpleName(),
            new HttpsEnforcer(configuration.isProxied()));
        httpsEnforcer.addMappingForUrlPatterns(null, true, "/*");
      }
    }
  }

  @VisibleForTesting
  static final class HttpsEnforcer extends AbstractHttpServletFilter {

    private static final String CONTENT_SECURITY_HEADER = "max-age=63072000; includeSubDomains; preload";
    private static final Pattern CR_OR_LF = Pattern.compile("\\r|\\n");

    private final boolean proxied;

    HttpsEnforcer(boolean proxied) {
      this.proxied = proxied;
    }

    @Override
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws IOException, ServletException {
      if (proxied) {
        if (StringUtils.isEmpty(request.getHeader(X_FORWARDED_PROTO))) {
          throw new IllegalStateException(
              "Configured to assume application is behind a proxy but the forward header has not been provided. "
                  + "Headers available: " + Headers.listForRequest(request).toList());
        }
        if (!request.getHeader(X_FORWARDED_PROTO).equalsIgnoreCase("https")) {
          switchToHttps(request, response);
          return;
        }
      } else {
        if (!request.isSecure()) {
          if (request.getProtocol().equalsIgnoreCase("https")) {
            throw new IllegalStateException(
                "Configured to assume application is accessed directly but connection is not secure and "
                    + "protocol is already https");
          }
          switchToHttps(request, response);
          return;
        }
      }
      response.addHeader(Headers.STRICT_CONTENT_SECURITY, CONTENT_SECURITY_HEADER);
      filterChain.doFilter(request, response);
    }

    private void switchToHttps(HttpServletRequest request, HttpServletResponse response) throws IOException {
      final StringBuffer url = new StringBuffer(128);
      URIUtil.appendSchemeHostPort(url, "https", request.getServerName(), request.getServerPort() == 80 ? 443 : request.getServerPort());
      url.append(request.getRequestURI());
      if (request.getQueryString() != null) {
        url.append("?");
        url.append(request.getQueryString());
      }
      String redirect = sanitize(url.toString());
      LOGGER.error("Unsecured access (url={}) redirected to [{}]", request.getRequestURL(), redirect);
      response.sendRedirect(redirect);
    }

    String sanitize(String url) {
      if (CR_OR_LF.matcher(url).find()) {
        throw new IllegalArgumentException("Attempted response split attack. Redirect URL = " + url);
      }
      return url;
    }
  }
}
