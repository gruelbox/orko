package com.grahamcrockford.orko.auth;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;

@RequestScoped
public class RequestUtils {
  
  private HttpServletRequest request;
  private AuthConfiguration authConfiguration;
  private Supplier<String> sourceIp;

  @Inject
  @VisibleForTesting
  public RequestUtils(HttpServletRequest request, AuthConfiguration authConfiguration) {
    this.request = request;
    this.authConfiguration = authConfiguration;
    this.sourceIp = Suppliers.memoize(() -> readSourceIp());
  }
  
  public String sourceIp() {
    return sourceIp.get();
  }
  
  private String readSourceIp() {
    if (authConfiguration.isProxied()) {
      String header = request.getHeader(Headers.X_FORWARDED_FOR);
      if (StringUtils.isEmpty(header)) {
        throw new IllegalStateException("Configured to assume application is behind a proxy but the forward header has not been provided. "
            + "Headers available: " + Headers.listForRequest(request).toList());
      }
      return header.split(",")[0];
    } else {
      return request.getRemoteAddr();
    }
  }
}