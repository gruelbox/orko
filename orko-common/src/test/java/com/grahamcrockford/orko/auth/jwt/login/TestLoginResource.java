package com.grahamcrockford.orko.auth.jwt.login;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.util.Optional;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.inject.util.Providers;
import com.grahamcrockford.orko.auth.AuthConfiguration;
import com.grahamcrockford.orko.auth.RequestUtils;
import com.grahamcrockford.orko.auth.blacklist.Blacklisting;


import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.testing.junit.ResourceTestRule;

public class TestLoginResource {

  private static AuthConfiguration authConfiguration = new AuthConfiguration();
  private static JwtLoginVerifier jwtLoginVerifier = mock(JwtLoginVerifier.class);
  private static TokenIssuer tokenIssuer = mock(TokenIssuer.class);
  private static RequestUtils requestUtils = mock(RequestUtils.class);
  
  static {
    authConfiguration.setAttemptsBeforeBlacklisting(5);
    authConfiguration.setBlacklistingExpirySeconds(3);
  }

  @ClassRule public static final ResourceTestRule resources = ResourceTestRule.builder()
      .addResource(new LoginResource(authConfiguration, jwtLoginVerifier, tokenIssuer, new Blacklisting(Providers.of(requestUtils), authConfiguration))).build();
  
  @Before
  public void setup() {
    when(requestUtils.sourceIp()).thenReturn("1.2.3.4");
  }

  @After
  public void tearDown() {
    reset(jwtLoginVerifier, tokenIssuer, requestUtils);
  }

  @Test
  public void testFail() throws AuthenticationException {
    when(jwtLoginVerifier.authenticate(Mockito.any(LoginRequest.class))).thenReturn(Optional.empty());
    Response response = failedLoginAttempt();
    assertEquals(403, response.getStatus());
  }
  
  @Test
  public void testLockout() throws AuthenticationException, InterruptedException {
    when(jwtLoginVerifier.authenticate(Mockito.any(LoginRequest.class))).thenReturn(Optional.empty());
    assertEquals(403, failedLoginAttempt().getStatus());
    assertEquals(403, failedLoginAttempt().getStatus());
    assertEquals(403, failedLoginAttempt().getStatus());
    assertEquals(403, failedLoginAttempt().getStatus());
    assertEquals(403, failedLoginAttempt().getStatus());
    assertEquals(429, failedLoginAttempt().getStatus());
    Thread.sleep(4000);
    assertEquals(403, failedLoginAttempt().getStatus());
    assertEquals(403, failedLoginAttempt().getStatus());
    assertEquals(403, failedLoginAttempt().getStatus());
    assertEquals(403, failedLoginAttempt().getStatus());
    assertEquals(403, failedLoginAttempt().getStatus());
    assertEquals(429, failedLoginAttempt().getStatus());
  }

  private Response failedLoginAttempt() {
    return resources.target("/auth/login").request().post(Entity.entity(new LoginRequest("", "", 0), MediaType.APPLICATION_JSON));
  }
}
