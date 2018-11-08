package com.grahamcrockford.orko.auth.jwt;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.jose4j.jws.AlgorithmIdentifiers.HMAC_SHA256;

import java.util.Optional;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.keys.HmacKey;
import org.jose4j.lang.JoseException;

import com.codahale.metrics.annotation.Timed;
import com.google.inject.Inject;
import com.grahamcrockford.orko.auth.AuthConfiguration;
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
  private final LoginAuthenticator loginAuthenticator;

  @Inject
  LoginResource(AuthConfiguration authConfiguration, LoginAuthenticator loginAuthenticator) {
    this.authConfiguration = authConfiguration;
    this.loginAuthenticator = loginAuthenticator;
  }

	@POST
	@Path("/login")
	@CacheControl(noCache = true, noStore = true, maxAge = 0)
	public final Response doLogin(LoginRequest basicCredentials) throws AuthenticationException, JoseException {
	  Optional<PrincipalImpl> principal = loginAuthenticator.authenticate(basicCredentials);
	  if (principal.isPresent()) {
	    return Response.ok().entity(new LoginResponse(buildToken(principal.get()).getCompactSerialization())).build();
	  } else {
	    return Response.status(Status.FORBIDDEN).entity(new LoginResponse()).build();
	  }
	}

  @GET
  @Path("/config")
  @Timed
  public Object getConfig() {
    return new Object();
  }

	private JsonWebSignature buildToken(PrincipalImpl user) {
		final JwtClaims claims = new JwtClaims();
		claims.setSubject(user.getName());
		claims.setStringClaim("roles", Roles.TRADER);
		claims.setExpirationTimeMinutesInTheFuture(authConfiguration.getJwt().getExpirationMinutes());
		claims.setIssuedAtToNow();
		claims.setGeneratedJwtId();

		final JsonWebSignature jws = new JsonWebSignature();
		jws.setPayload(claims.toJson());
		jws.setAlgorithmHeaderValue(HMAC_SHA256);
		jws.setKey(new HmacKey(authConfiguration.getJwt().getSecretBytes()));
		return jws;
	}
}