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
package com.gruelbox.orko.auth.jwt;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.google.common.base.Charsets;
import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.Scope;
import com.google.inject.servlet.RequestScoped;
import com.gruelbox.orko.auth.AuthConfiguration;
import com.gruelbox.orko.auth.AuthModule;
import com.gruelbox.orko.auth.GoogleAuthenticatorModule;
import com.gruelbox.orko.auth.Headers;
import com.gruelbox.orko.auth.Roles;
import com.gruelbox.orko.auth.jwt.login.TokenIssuer;
import io.dropwizard.auth.PrincipalImpl;
import java.io.IOException;
import java.util.UUID;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.lang.JoseException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class TestJwtAuthentication {

  private AuthConfiguration config;

  private JwtXsrfProtectionFilter xsrfFilter;
  private JwtAuthenticationFilter authFilter;

  @Mock private HttpServletResponse response;
  @Mock private HttpServletRequest request;
  @Mock private FilterChain chain;

  @Before
  public void setup() {

    MockitoAnnotations.initMocks(this);

    config = new AuthConfiguration();
    config.setJwt(new JwtConfiguration());
    config.getJwt().setUserName("user");
    config.getJwt().setPassword("pass");
    config.getJwt().setSecret(UUID.randomUUID().toString());
    config.getJwt().setExpirationMinutes(60);

    Injector injector =
        Guice.createInjector(
            new Module() {
              @Override
              public void configure(Binder binder) {
                binder.bind(HttpServletRequest.class).toInstance(request);
                binder.bindScope(
                    RequestScoped.class,
                    new Scope() {
                      @Override
                      public <T> Provider<T> scope(Key<T> key, Provider<T> unscoped) {
                        return unscoped;
                      }
                    });
                binder.bind(AuthConfiguration.class).toInstance(config);
              }
            },
            new GoogleAuthenticatorModule(),
            new AuthModule.Testing(),
            new JwtModule(config));

    xsrfFilter = injector.getInstance(JwtXsrfProtectionFilter.class);
    authFilter = injector.getInstance(JwtAuthenticationFilter.class);
  }

  @Test
  public void testNoAccessTokenXsrf() throws IOException, ServletException {
    when(request.getCookies()).thenReturn(new Cookie[] {});
    xsrfFilter.doFilter(request, response, chain);
    verify(response).sendError(401);
    verifyNoInteractions(chain);
  }

  @Test
  public void testMissingXsrf() throws IOException, ServletException, JoseException {
    when(request.getCookies())
        .thenReturn(new Cookie[] {new Cookie("accessToken", validJwt().getCompactSerialization())});
    xsrfFilter.doFilter(request, response, chain);
    verify(response).sendError(401);
    verifyNoInteractions(chain);
  }

  @Test
  public void testInvalidXsrf() throws IOException, ServletException, JoseException {
    when(request.getCookies())
        .thenReturn(new Cookie[] {new Cookie("accessToken", validJwt().getCompactSerialization())});
    when(request.getHeader(Headers.X_XSRF_TOKEN)).thenReturn("XXX");
    xsrfFilter.doFilter(request, response, chain);
    verify(response).sendError(401);
    verifyNoInteractions(chain);
  }

  @Test
  public void testValidXsrf()
      throws IOException, ServletException, JoseException, MalformedClaimException {
    TokenIssuer issuer = new TokenIssuer(config.getJwt().getSecretBytes(), 60);
    JwtClaims claims = issuer.buildClaims(new PrincipalImpl("FOO"), Roles.TRADER);
    JsonWebSignature validJwt = issuer.claimsToToken(claims);
    when(request.getCookies())
        .thenReturn(new Cookie[] {new Cookie("accessToken", validJwt.getCompactSerialization())});
    when(request.getHeader(Headers.X_XSRF_TOKEN))
        .thenReturn(claims.getClaimValue("xsrf", String.class));
    xsrfFilter.doFilter(request, response, chain);
    verifyNoInteractions(response);
    verify(chain).doFilter(request, response);
  }

  @Test
  public void testNoAccessTokenAuth() throws IOException, ServletException {
    when(request.getCookies()).thenReturn(new Cookie[] {});
    authFilter.doFilter(request, response, chain);
    verify(response).sendError(401);
    verifyNoInteractions(chain);
  }

  @Test
  public void testMalformedJwt() throws IOException, ServletException {
    when(request.getCookies()).thenReturn(new Cookie[] {new Cookie("accessToken", "nonsense")});
    authFilter.doFilter(request, response, chain);
    verify(response).sendError(401);
    verifyNoInteractions(chain);
  }

  @Test
  public void testForgedJwt() throws IOException, ServletException, JoseException {
    String forgedToken =
        new TokenIssuer(UUID.randomUUID().toString().getBytes(Charsets.UTF_8), 60)
            .buildToken(new PrincipalImpl("FOO"), Roles.TRADER)
            .getCompactSerialization();
    when(request.getCookies()).thenReturn(new Cookie[] {new Cookie("accessToken", forgedToken)});
    authFilter.doFilter(request, response, chain);
    verify(response).sendError(401);
    verifyNoInteractions(chain);
  }

  @Test
  public void testWrongRole() throws IOException, ServletException, JoseException {
    String forgedToken =
        new TokenIssuer(config.getJwt().getSecretBytes(), 60)
            .buildToken(new PrincipalImpl("FOO"), "SOMETHINGELSE")
            .getCompactSerialization();
    when(request.getCookies()).thenReturn(new Cookie[] {new Cookie("accessToken", forgedToken)});
    authFilter.doFilter(request, response, chain);
    verify(response).sendError(401);
    verifyNoInteractions(chain);
  }

  @Test
  public void testNoRoles() throws IOException, ServletException, JoseException {
    String forgedToken =
        new TokenIssuer(config.getJwt().getSecretBytes(), 60)
            .buildToken(new PrincipalImpl("FOO"), "")
            .getCompactSerialization();
    when(request.getCookies()).thenReturn(new Cookie[] {new Cookie("accessToken", forgedToken)});
    authFilter.doFilter(request, response, chain);
    verify(response).sendError(401);
    verifyNoInteractions(chain);
  }

  @Test
  public void testValid() throws IOException, ServletException, JoseException {
    when(request.getCookies())
        .thenReturn(new Cookie[] {new Cookie("accessToken", validJwt().getCompactSerialization())});
    authFilter.doFilter(request, response, chain);
    verifyNoInteractions(response);
    verify(chain).doFilter(request, response);
  }

  private JsonWebSignature validJwt() {
    return new TokenIssuer(config.getJwt().getSecretBytes(), 60)
        .buildToken(new PrincipalImpl("FOO"), Roles.TRADER);
  }
}
