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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import com.google.inject.util.Providers;
import com.gruelbox.orko.auth.AuthConfiguration;
import com.gruelbox.orko.auth.RequestUtils;
import com.gruelbox.orko.auth.blacklist.Blacklisting;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.testing.junit.ResourceTestRule;
import java.util.Optional;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mockito;

public class TestLoginResource {

  private static AuthConfiguration authConfiguration = new AuthConfiguration();
  private static JwtLoginVerifier jwtLoginVerifier = mock(JwtLoginVerifier.class);
  private static TokenIssuer tokenIssuer = mock(TokenIssuer.class);
  private static RequestUtils requestUtils = mock(RequestUtils.class);
  private static Blacklisting blacklisting;

  static {
    authConfiguration.setAttemptsBeforeBlacklisting(5);
    authConfiguration.setBlacklistingExpirySeconds(3);
    blacklisting = new Blacklisting(Providers.of(requestUtils), authConfiguration);
  }

  @ClassRule
  public static final ResourceTestRule resources =
      ResourceTestRule.builder()
          .addResource(
              new LoginResource(authConfiguration, jwtLoginVerifier, tokenIssuer, blacklisting))
          .build();

  @Before
  public void setup() {
    when(requestUtils.sourceIp()).thenReturn("1.2.3.4");
    blacklisting.success();
  }

  @After
  public void tearDown() {
    reset(jwtLoginVerifier, tokenIssuer, requestUtils);
  }

  @Test
  public void testFail() throws AuthenticationException {
    when(jwtLoginVerifier.authenticate(Mockito.any(LoginRequest.class)))
        .thenReturn(Optional.empty());
    Response response = failedLoginAttempt();
    assertEquals(403, response.getStatus());
  }

  @Test
  public void testLockout() throws AuthenticationException, InterruptedException {
    when(jwtLoginVerifier.authenticate(Mockito.any(LoginRequest.class)))
        .thenReturn(Optional.empty());
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
    blacklisting.cleanUp();
    return resources
        .target("/auth/login")
        .request()
        .post(Entity.entity(new LoginRequest("", "", 0), MediaType.APPLICATION_JSON));
  }
}
