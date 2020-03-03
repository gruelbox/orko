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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.inject.util.Providers;
import com.gruelbox.orko.auth.AuthConfiguration;
import com.gruelbox.orko.auth.Headers;
import com.gruelbox.orko.auth.RequestUtils;
import com.gruelbox.orko.db.Transactionally;
import com.warrenstrange.googleauth.IGoogleAuthenticator;
import java.util.Enumeration;
import javax.servlet.http.HttpServletRequest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class TestIpWhitelisting {

  private static final String SECRET_KEY = "XXX";
  private static final String MOST_RECENT_FORWARD = "MostRecentForward";
  private static final String ORIGIN = "Origin";

  private IpWhitelistingService ontest;

  @Mock private HttpServletRequest request;
  @Mock private IGoogleAuthenticator googleAuthenticator;
  private final AuthConfiguration configuration = new AuthConfiguration();
  @Mock private IpWhitelistAccess ipWhitelistAccess;
  @Mock private Transactionally transactionally;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);

    when(request.getRemoteAddr()).thenReturn(ORIGIN);

    ontest =
        new IpWhitelistingService(
            Providers.of(new RequestUtils(request, configuration)),
            googleAuthenticator,
            configuration,
            Providers.of(ipWhitelistAccess));
  }

  @Test
  public void testDisabledNoConfig() {
    configuration.setIpWhitelisting(null);
    assertDisabled();
  }

  @Test
  public void testDisabledNullConfig() {
    IpWhitelistingConfiguration ipWhitelistingConfiguration = new IpWhitelistingConfiguration();
    ipWhitelistingConfiguration.setSecretKey(null);
    configuration.setIpWhitelisting(ipWhitelistingConfiguration);
    assertDisabled();
  }

  @Test
  public void testDisabledBlankConfig() {
    IpWhitelistingConfiguration ipWhitelistingConfiguration = new IpWhitelistingConfiguration();
    ipWhitelistingConfiguration.setSecretKey("");
    configuration.setIpWhitelisting(ipWhitelistingConfiguration);
    assertDisabled();
  }

  @Test
  public void testAuthoriseDirectIpFalse() {
    enabled();
    when(ipWhitelistAccess.exists(ORIGIN)).thenReturn(false);
    assertFalse(ontest.authoriseIp());
  }

  @Test
  public void testAuthoriseDirectIpTrue() {
    enabled();
    when(ipWhitelistAccess.exists(ORIGIN)).thenReturn(true);
    assertTrue(ontest.authoriseIp());
  }

  @Test
  public void testAuthoriseProxiedLongChainFalse() {
    enabled();
    when(ipWhitelistAccess.exists(MOST_RECENT_FORWARD)).thenReturn(false);
    configuration.setProxied(true);
    when(request.getHeader(Headers.X_FORWARDED_FOR))
        .thenReturn(MOST_RECENT_FORWARD + ",OldForward,OlderForward");
    assertFalse(ontest.authoriseIp());
  }

  @Test
  public void testAuthoriseProxiedLongChainTrue() {
    enabled();
    when(ipWhitelistAccess.exists(MOST_RECENT_FORWARD)).thenReturn(true);
    configuration.setProxied(true);
    when(request.getHeader(Headers.X_FORWARDED_FOR))
        .thenReturn(MOST_RECENT_FORWARD + ",OldForward,OlderForward");
    assertTrue(ontest.authoriseIp());
  }

  @Test
  public void testAuthoriseProxiedShortChainFalse() {
    enabled();
    when(ipWhitelistAccess.exists(MOST_RECENT_FORWARD)).thenReturn(false);
    configuration.setProxied(true);
    when(request.getHeader(Headers.X_FORWARDED_FOR)).thenReturn(MOST_RECENT_FORWARD);
    assertFalse(ontest.authoriseIp());
  }

  @Test
  public void testAuthoriseProxiedShortChainTrue() {
    enabled();
    when(ipWhitelistAccess.exists(MOST_RECENT_FORWARD)).thenReturn(true);
    configuration.setProxied(true);
    when(request.getHeader(Headers.X_FORWARDED_FOR)).thenReturn(MOST_RECENT_FORWARD);
    assertTrue(ontest.authoriseIp());
  }

  @Test
  public void testWhitelistDirectIpFalse() {
    enabled();
    when(googleAuthenticator.authorize(SECRET_KEY, 1234)).thenReturn(false);
    assertFalse(ontest.whiteListRequestIp(1234));
    verify(ipWhitelistAccess, times(0)).add(Mockito.anyString());
  }

  @Test
  public void testWhitelistDirectIpTrue() {
    enabled();
    when(googleAuthenticator.authorize(SECRET_KEY, 1234)).thenReturn(true);
    assertTrue(ontest.whiteListRequestIp(1234));
    verify(ipWhitelistAccess).add(ORIGIN);
  }

  @Test
  public void testWhitelistProxiedFalse() {
    enabled();
    when(request.getHeader(Headers.X_FORWARDED_FOR)).thenReturn(MOST_RECENT_FORWARD);
    configuration.setProxied(true);
    when(googleAuthenticator.authorize(SECRET_KEY, 1234)).thenReturn(false);
    assertFalse(ontest.whiteListRequestIp(1234));
    verify(ipWhitelistAccess, times(0)).add(Mockito.anyString());
  }

  @Test
  public void testWhitelistProxiedTrue() {
    enabled();
    when(request.getHeader(Headers.X_FORWARDED_FOR)).thenReturn(MOST_RECENT_FORWARD);
    configuration.setProxied(true);
    when(googleAuthenticator.authorize(SECRET_KEY, 1234)).thenReturn(true);
    assertTrue(ontest.whiteListRequestIp(1234));
    verify(ipWhitelistAccess).add(MOST_RECENT_FORWARD);
  }

  @Test(expected = IllegalStateException.class)
  public void testWhitelistProxiedNoForwardHeader() {
    enabled();
    when(request.getHeader(Headers.X_FORWARDED_FOR)).thenReturn(null);
    when(request.getHeaderNames())
        .thenReturn(
            new Enumeration<String>() {
              @Override
              public String nextElement() {
                return null;
              }

              @Override
              public boolean hasMoreElements() {
                return false;
              }
            });
    configuration.setProxied(true);
    when(googleAuthenticator.authorize(SECRET_KEY, 1234)).thenReturn(true);
    ontest.whiteListRequestIp(1234);
  }

  @Test
  public void testDeWhitelistIpDoesNotExist() {
    enabled();
    when(ipWhitelistAccess.exists(ORIGIN)).thenReturn(false);
    assertFalse(ontest.deWhitelistIp());
    verify(ipWhitelistAccess, times(0)).delete(Mockito.anyString());
  }

  @Test
  public void testDeWhitelistIpExists() {
    enabled();
    when(ipWhitelistAccess.exists(ORIGIN)).thenReturn(true);
    assertTrue(ontest.deWhitelistIp());
    verify(ipWhitelistAccess).delete(ORIGIN);
  }

  private void assertDisabled() {
    assertTrue(ontest.authoriseIp());
    assertTrue(ontest.whiteListRequestIp(1234));
    assertFalse(ontest.deWhitelistIp());
  }

  private void enabled() {
    IpWhitelistingConfiguration ipWhitelistingConfiguration = new IpWhitelistingConfiguration();
    ipWhitelistingConfiguration.setSecretKey(SECRET_KEY);
    configuration.setIpWhitelisting(ipWhitelistingConfiguration);
  }
}
