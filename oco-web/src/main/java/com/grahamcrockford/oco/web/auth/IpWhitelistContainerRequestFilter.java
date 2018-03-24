package com.grahamcrockford.oco.web.auth;

import java.io.IOException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
class IpWhitelistContainerRequestFilter implements ContainerRequestFilter {

  private final IpWhitelisting ipWhitelisting;

  @Inject
  IpWhitelistContainerRequestFilter(IpWhitelisting ipWhitelisting) {
    this.ipWhitelisting = ipWhitelisting;
  }

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {
    if (!requestContext.getUriInfo().getPath().equals("auth") && !ipWhitelisting.authoriseIp()) {
      requestContext.abortWith(Response.status(403).build());
    }
  }
}
