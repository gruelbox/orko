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
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mockito;

import com.grahamcrockford.orko.auth.AuthConfiguration;

import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.testing.junit.ResourceTestRule;

public class TestLoginResource {

  private static AuthConfiguration authConfiguration = mock(AuthConfiguration.class);
  private static JwtLoginVerifier jwtLoginVerifier = mock(JwtLoginVerifier.class);
  private static TokenIssuer tokenIssuer = mock(TokenIssuer.class);

  @ClassRule public static final ResourceTestRule resources = ResourceTestRule.builder()
      .addResource(new LoginResource(authConfiguration, jwtLoginVerifier, tokenIssuer)).build();

  @After
  public void tearDown() {
    reset(authConfiguration, jwtLoginVerifier, tokenIssuer);
  }

  @Test
  public void testFail() throws AuthenticationException {
    when(jwtLoginVerifier.authenticate(Mockito.any(LoginRequest.class))).thenReturn(Optional.empty());
    Response response = resources.target("/auth/login").request().post(Entity.entity(new LoginRequest("", "", 0), MediaType.APPLICATION_JSON));
    assertEquals(403, response.getStatus());
  }
  
  @Test
  public void testLockout() throws AuthenticationException, InterruptedException {
    when(jwtLoginVerifier.authenticate(Mockito.any(LoginRequest.class))).thenReturn(Optional.empty());
    Response response = resources.target("/auth/login").request().post(Entity.entity(new LoginRequest("", "", 0), MediaType.APPLICATION_JSON));
    assertEquals(403, response.getStatus());
    assertEquals(403, response.getStatus());
    assertEquals(403, response.getStatus());
    assertEquals(403, response.getStatus());
    assertEquals(403, response.getStatus());
    assertEquals(404, response.getStatus());
    Thread.sleep(30000);
    assertEquals(403, response.getStatus());
    assertEquals(403, response.getStatus());
    assertEquals(403, response.getStatus());
    assertEquals(403, response.getStatus());
    assertEquals(403, response.getStatus());
    assertEquals(404, response.getStatus());
  }
}
