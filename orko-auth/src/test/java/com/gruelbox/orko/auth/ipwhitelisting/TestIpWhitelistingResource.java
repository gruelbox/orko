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
package com.gruelbox.orko.auth.ipwhitelisting;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import com.google.inject.util.Providers;
import com.gruelbox.orko.auth.AuthConfiguration;
import com.gruelbox.orko.auth.RequestUtils;
import com.gruelbox.orko.auth.blacklist.Blacklisting;
import io.dropwizard.testing.junit.ResourceTestRule;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

public class TestIpWhitelistingResource {

  private static AuthConfiguration authConfiguration = new AuthConfiguration();
  private static final IpWhitelistingService ipWhitelisting = mock(IpWhitelistingService.class);
  private static final RequestUtils requestUtils = mock(RequestUtils.class);
  private static final Blacklisting blackListing;

  static {
    authConfiguration.setAttemptsBeforeBlacklisting(5);
    authConfiguration.setBlacklistingExpirySeconds(3);
    blackListing = new Blacklisting(Providers.of(requestUtils), authConfiguration);
  }

  @ClassRule
  public static final ResourceTestRule resources =
      ResourceTestRule.builder()
          .addResource(new IpWhitelistingResource(ipWhitelisting, blackListing))
          .build();

  @Before
  public void setup() {
    reset(ipWhitelisting, requestUtils);
    when(requestUtils.sourceIp()).thenReturn("1.2.3.4");
    blackListing.success();
  }

  @After
  public void tearDown() {
    reset(ipWhitelisting);
  }

  @Test
  public void testGetFail() {
    when(ipWhitelisting.authoriseIp()).thenReturn(false);
    Boolean result = resources.target("/auth").request().get(Boolean.class);
    assertFalse(result);
  }

  @Test
  public void testGetSuccess() {
    when(ipWhitelisting.authoriseIp()).thenReturn(true);
    Boolean result = resources.target("/auth").request().get(Boolean.class);
    assertTrue(result);
  }

  @Test
  public void testDeleteFail() {
    when(ipWhitelisting.deWhitelistIp()).thenReturn(false);
    Response result = resources.target("/auth").request().delete();
    assertEquals(Response.Status.FORBIDDEN.getStatusCode(), result.getStatus());
  }

  @Test
  public void testDeleteSuccess() {
    when(ipWhitelisting.deWhitelistIp()).thenReturn(true);
    Response result = resources.target("/auth").request().delete();
    assertEquals(Response.Status.OK.getStatusCode(), result.getStatus());
  }

  @Test
  public void testPutFail() throws InterruptedException {
    when(ipWhitelisting.whiteListRequestIp(1234)).thenReturn(false);
    assertEquals(Response.Status.FORBIDDEN.getStatusCode(), loginAttempt().getStatus());
    assertEquals(Response.Status.FORBIDDEN.getStatusCode(), loginAttempt().getStatus());
    assertEquals(Response.Status.FORBIDDEN.getStatusCode(), loginAttempt().getStatus());
    assertEquals(Response.Status.FORBIDDEN.getStatusCode(), loginAttempt().getStatus());
    assertEquals(Response.Status.FORBIDDEN.getStatusCode(), loginAttempt().getStatus());
    assertEquals(Response.Status.TOO_MANY_REQUESTS.getStatusCode(), loginAttempt().getStatus());
    Thread.sleep(3500);
    assertEquals(Response.Status.FORBIDDEN.getStatusCode(), loginAttempt().getStatus());
    assertEquals(Response.Status.FORBIDDEN.getStatusCode(), loginAttempt().getStatus());
    assertEquals(Response.Status.FORBIDDEN.getStatusCode(), loginAttempt().getStatus());
    assertEquals(Response.Status.FORBIDDEN.getStatusCode(), loginAttempt().getStatus());
    assertEquals(Response.Status.FORBIDDEN.getStatusCode(), loginAttempt().getStatus());
    assertEquals(Response.Status.TOO_MANY_REQUESTS.getStatusCode(), loginAttempt().getStatus());
    when(requestUtils.sourceIp()).thenReturn("1.2.3.5");
    assertEquals(Response.Status.FORBIDDEN.getStatusCode(), loginAttempt().getStatus());
  }

  @Test
  public void testDDoS() throws InterruptedException {
    when(ipWhitelisting.whiteListRequestIp(1234)).thenReturn(false);
    for (int i = 0; i < 100; i++) {
      when(requestUtils.sourceIp()).thenReturn("ip" + i);
      assertEquals(Response.Status.FORBIDDEN.getStatusCode(), loginAttemptNoCleanup().getStatus());
    }
    when(requestUtils.sourceIp()).thenReturn("ip100");
    assertEquals(
        Response.Status.TOO_MANY_REQUESTS.getStatusCode(), loginAttemptNoCleanup().getStatus());
    assertEquals(
        Response.Status.TOO_MANY_REQUESTS.getStatusCode(), loginAttemptNoCleanup().getStatus());
    assertEquals(
        Response.Status.TOO_MANY_REQUESTS.getStatusCode(), loginAttemptNoCleanup().getStatus());
  }

  @Test
  public void testPutSuccess() {
    when(ipWhitelisting.whiteListRequestIp(1234)).thenReturn(true);
    Response result = loginAttempt();
    assertEquals(Response.Status.OK.getStatusCode(), result.getStatus());
  }

  private Response loginAttempt() {
    blackListing.cleanUp();
    return loginAttemptNoCleanup();
  }

  private Response loginAttemptNoCleanup() {
    return resources
        .target("/auth")
        .queryParam("token", 1234)
        .request()
        .put(Entity.entity("", MediaType.TEXT_PLAIN));
  }
}
