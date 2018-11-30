package com.gruelbox.orko.allinone;

import static com.google.common.net.HttpHeaders.CONTENT_SECURITY_POLICY;
import static com.google.common.net.HttpHeaders.X_CONTENT_TYPE_OPTIONS;
import static com.google.common.net.HttpHeaders.X_FRAME_OPTIONS;
import static com.google.common.net.HttpHeaders.X_XSS_PROTECTION;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.util.URIUtil;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.net.HttpHeaders;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.gruelbox.orko.auth.AbstractHttpServletFilter;
import com.gruelbox.orko.auth.AuthConfiguration;

@Singleton
class ClientSecurityHeadersFilter extends AbstractHttpServletFilter {

  private static final String X_CONTENT_SECURITY_POLICY = "X-Content-Security-Policy";
  private static final String IE10 = "MSIE 10";
  private static final String IE11 = "rv:11.0";

  private Supplier<String> contentSecurityPolicy;

  @Inject
  ClientSecurityHeadersFilter(Provider<HttpServletRequest> httpServletRequest, Provider<AuthConfiguration> authConfiguration) {
    this.contentSecurityPolicy = Suppliers.memoize(() -> {
      final StringBuffer wssUri = new StringBuffer(128);
      URIUtil.appendSchemeHostPort(wssUri,
          authConfiguration.get().isHttpsOnly() ? "wss" : "ws",
          httpServletRequest.get().getServerName(),
          httpServletRequest.get().getServerPort());
      return "default-src 'none'; "
          + "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; "
          + "font-src 'self' https://fonts.gstatic.com data:; "
          + "script-src 'self' https://*.tradingview.com; "
          + "img-src 'self' data:; "
          + "frame-src 'self' https://*.tradingview.com; "
          + "connect-src 'self' " + wssUri.toString() + "; "
          + "manifest-src 'self'; "
          + "frame-ancestors 'self';";
    });
  }

  @Override
  public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
    response.setHeader(X_FRAME_OPTIONS, "sameorigin");
    response.setHeader(X_XSS_PROTECTION, "1; mode=block");
    response.setHeader(CONTENT_SECURITY_POLICY, contentSecurityPolicy.get());
    response.setHeader(X_CONTENT_TYPE_OPTIONS, "nosniff");
    String userAgent = request.getHeader(HttpHeaders.USER_AGENT);
    if (userAgent != null) {
      if (userAgent.contains(IE10) || userAgent.contains(IE11)) {
        response.setHeader(X_CONTENT_SECURITY_POLICY, contentSecurityPolicy.get());
      }
    }
    chain.doFilter(request, response);
  }
}