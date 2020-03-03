/**
 * Orko - Copyright © 2018-2019 Graham Crockford
 *
 * <p>This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * <p>You should have received a copy of the GNU Affero General Public License along with this
 * program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.gruelbox.orko.auth.jwt.login;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.gruelbox.orko.auth.AuthConfiguration;
import com.gruelbox.orko.auth.CookieHandlers;
import com.gruelbox.orko.auth.Roles;
import com.gruelbox.orko.auth.blacklist.Blacklisting;
import com.gruelbox.tools.dropwizard.guice.resources.WebResource;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.PrincipalImpl;
import io.dropwizard.jersey.caching.CacheControl;
import java.util.Map;
import java.util.Optional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.lang.JoseException;

@Path("auth")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@Singleton
public class LoginResource implements WebResource {

  private final AuthConfiguration authConfiguration;
  private final JwtLoginVerifier jwtLoginVerifier;
  private final TokenIssuer tokenIssuer;
  private Blacklisting blacklisting;

  @Inject
  LoginResource(
      AuthConfiguration authConfiguration,
      JwtLoginVerifier jwtLoginVerifier,
      TokenIssuer tokenIssuer,
      Blacklisting blacklisting) {
    this.authConfiguration = authConfiguration;
    this.jwtLoginVerifier = jwtLoginVerifier;
    this.tokenIssuer = tokenIssuer;
    this.blacklisting = blacklisting;
  }

  @GET
  @Path("/login")
  @CacheControl(noCache = true, noStore = true, maxAge = 0)
  public final Response getLogin(
      @QueryParam("username") String username,
      @QueryParam("password") String password,
      @QueryParam("secondfactor") int secondFactor)
      throws AuthenticationException, JoseException {
    return doLogin(new LoginRequest(username, password, secondFactor));
  }

  @POST
  @Path("/login")
  @CacheControl(noCache = true, noStore = true, maxAge = 0)
  public final Response doLogin(LoginRequest loginRequest)
      throws AuthenticationException, JoseException {
    if (blacklisting.isBlacklisted()) {
      blacklisting.failure();
      return Response.status(Status.TOO_MANY_REQUESTS).entity(new LoginResponse()).build();
    }

    Optional<PrincipalImpl> principal = jwtLoginVerifier.authenticate(loginRequest);
    if (!principal.isPresent()) {
      blacklisting.failure();
      return Response.status(Status.FORBIDDEN).entity(new LoginResponse()).build();
    }

    blacklisting.success();

    JwtClaims claims = tokenIssuer.buildClaims(principal.get(), Roles.TRADER);
    String token = tokenIssuer.claimsToToken(claims).getCompactSerialization();
    String xsrf = (String) claims.getClaimValue(TokenIssuer.XSRF_CLAIM);
    return Response.ok()
        .cookie(CookieHandlers.ACCESS_TOKEN.create(token, authConfiguration))
        .entity(new LoginResponse(authConfiguration.getJwt().getExpirationMinutes(), xsrf))
        .build();
  }

  @GET
  @Path("/config")
  @Timed
  public Map<String, String> getConfig() {
    return ImmutableMap.of();
  }
}
