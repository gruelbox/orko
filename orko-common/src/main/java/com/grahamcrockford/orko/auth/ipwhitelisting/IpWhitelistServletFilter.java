package com.grahamcrockford.orko.auth.ipwhitelisting;

import java.io.IOException;

import javax.annotation.Priority;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.grahamcrockford.orko.auth.AbstractHttpSecurityServletFilter;

/**
 * Container-level filter which only allows access to the API if
 * the origin IP has been whitelisted.  At container level we mop up REST resources, web
 * sockets, static resources etc.
 */
@Singleton
@Priority(100)
class IpWhitelistServletFilter extends AbstractHttpSecurityServletFilter {

  private final IpWhitelisting ipWhitelisting;

  @Inject
  IpWhitelistServletFilter(IpWhitelisting ipWhitelisting) {
    this.ipWhitelisting = ipWhitelisting;
  }

  @Override
  protected boolean filterHttpRequest(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
    if (ipWhitelisting.authoriseIp()) {
      return true;
    }

    response.sendError(Response.Status.FORBIDDEN.getStatusCode());
    return false;
  }
}
