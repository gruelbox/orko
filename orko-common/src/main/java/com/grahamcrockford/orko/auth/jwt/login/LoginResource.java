package com.grahamcrockford.orko.auth.jwt.login;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
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

import org.jose4j.lang.JoseException;

import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.grahamcrockford.orko.auth.AuthConfiguration;
import com.grahamcrockford.orko.auth.CookieHandlers;
import com.grahamcrockford.orko.auth.Roles;
import com.grahamcrockford.orko.wiring.WebResource;

import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.PrincipalImpl;
import io.dropwizard.jersey.caching.CacheControl;

@Path("auth")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
public class LoginResource implements WebResource {

  private final AuthConfiguration authConfiguration;
  private final JwtLoginVerifier jwtLoginVerifier;
  private final TokenIssuer tokenIssuer;

  @Inject
  LoginResource(AuthConfiguration authConfiguration, JwtLoginVerifier jwtLoginVerifier, TokenIssuer tokenIssuer) {
    this.authConfiguration = authConfiguration;
    this.jwtLoginVerifier = jwtLoginVerifier;
    this.tokenIssuer = tokenIssuer;
  }

  @GET
  @Path("/login")
  @CacheControl(noCache = true, noStore = true, maxAge = 0)
  public final Response getLogin(@QueryParam("username") String username, @QueryParam("password") String password, @QueryParam("secondfactor") int secondFactor) throws AuthenticationException, JoseException {
    return doLogin(new LoginRequest(username, password, secondFactor));
  }

  @POST
  @Path("/login")
  @CacheControl(noCache = true, noStore = true, maxAge = 0)
  public final Response doLogin(LoginRequest loginRequest) throws AuthenticationException, JoseException {
    Optional<PrincipalImpl> principal = jwtLoginVerifier.authenticate(loginRequest);
    if (principal.isPresent()) {
      String token = tokenIssuer.buildToken(principal.get(), Roles.TRADER).getCompactSerialization();
      return Response.ok().cookie(CookieHandlers.ACCESS_TOKEN.create(token, authConfiguration))
          .entity(new LoginResponse(authConfiguration.getJwt().getExpirationMinutes())).build();
    } else {
      return Response.status(Status.FORBIDDEN).entity(new LoginResponse()).build();
    }
  }

  @GET
  @Path("/config")
  @Timed
  public Map<String, String> getConfig() {
    return ImmutableMap.of();
  }
}