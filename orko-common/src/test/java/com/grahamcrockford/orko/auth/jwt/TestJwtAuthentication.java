package com.grahamcrockford.orko.auth.jwt;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

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

import com.google.common.base.Charsets;
import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.Scope;
import com.google.inject.servlet.RequestScoped;
import com.grahamcrockford.orko.OrkoConfiguration;
import com.grahamcrockford.orko.auth.AuthConfiguration;
import com.grahamcrockford.orko.auth.AuthModule;
import com.grahamcrockford.orko.auth.GoogleAuthenticatorModule;
import com.grahamcrockford.orko.auth.Headers;
import com.grahamcrockford.orko.auth.Roles;
import com.grahamcrockford.orko.auth.jwt.login.TokenIssuer;

import io.dropwizard.auth.PrincipalImpl;

public class TestJwtAuthentication {

  private OrkoConfiguration config;

  private JwtXsrfProtectionFilter xsrfFilter;
  private JwtAuthenticationFilter authFilter;

  @Mock private HttpServletResponse response;
  @Mock private HttpServletRequest request;
  @Mock private FilterChain chain;

  @Before
  public void setup() {

    MockitoAnnotations.initMocks(this);

    config = new OrkoConfiguration();
    config.setAuth(new AuthConfiguration());
    config.getAuth().setJwt(new JwtConfiguration());
    config.getAuth().getJwt().setUserName("user");
    config.getAuth().getJwt().setPassword("pass");
    config.getAuth().getJwt().setSecret(UUID.randomUUID().toString());
    config.getAuth().getJwt().setExpirationMinutes(60);

    Injector injector = Guice.createInjector(
      new Module() {
        @Override
        public void configure(Binder binder) {
          binder.bind(OrkoConfiguration.class).toInstance(config);
          binder.bind(HttpServletRequest.class).toInstance(request);
          binder.bindScope(RequestScoped.class, new Scope() {
            @Override
            public <T> Provider<T> scope(Key<T> key, Provider<T> unscoped) {
              return unscoped;
            }
          });
        }
      },
      new GoogleAuthenticatorModule(),
      new AuthModule.Testing(),
      new JwtModule(config.getAuth())
    );

    xsrfFilter = injector.getInstance(JwtXsrfProtectionFilter.class);
    authFilter = injector.getInstance(JwtAuthenticationFilter.class);
  }

  @Test
  public void testNoAccessTokenXsrf() throws IOException, ServletException {
    when(request.getCookies()).thenReturn(new Cookie[] { });
    xsrfFilter.doFilter(request, response, chain);
    verify(response).sendError(401);
    verifyZeroInteractions(chain);
  }

  @Test
  public void testMissingXsrf() throws IOException, ServletException, JoseException {
    when(request.getCookies()).thenReturn(new Cookie[] { new Cookie("accessToken", validJwt().getCompactSerialization()) });
    xsrfFilter.doFilter(request, response, chain);
    verify(response).sendError(401);
    verifyZeroInteractions(chain);
  }

  @Test
  public void testInvalidXsrf() throws IOException, ServletException, JoseException {
    when(request.getCookies()).thenReturn(new Cookie[] { new Cookie("accessToken", validJwt().getCompactSerialization()) });
    when(request.getHeader(Headers.X_XSRF_TOKEN)).thenReturn("XXX");
    xsrfFilter.doFilter(request, response, chain);
    verify(response).sendError(401);
    verifyZeroInteractions(chain);
  }

  @Test
  public void testValidXsrf() throws IOException, ServletException, JoseException, MalformedClaimException {
    TokenIssuer issuer = new TokenIssuer(config.getAuth().getJwt().getSecretBytes(), 60);
    JwtClaims claims = issuer.buildClaims(new PrincipalImpl("FOO"), Roles.TRADER);
    JsonWebSignature validJwt = issuer.claimsToToken(claims);
    when(request.getCookies()).thenReturn(new Cookie[] { new Cookie("accessToken", validJwt.getCompactSerialization()) });
    when(request.getHeader(Headers.X_XSRF_TOKEN)).thenReturn(claims.getClaimValue("xsrf", String.class));
    xsrfFilter.doFilter(request, response, chain);
    verifyZeroInteractions(response);
    verify(chain).doFilter(request, response);
  }

  @Test
  public void testNoAccessTokenAuth() throws IOException, ServletException {
    when(request.getCookies()).thenReturn(new Cookie[] { });
    authFilter.doFilter(request, response, chain);
    verify(response).sendError(401);
    verifyZeroInteractions(chain);
  }

  @Test
  public void testMalformedJwt() throws IOException, ServletException {
    when(request.getCookies()).thenReturn(new Cookie[] { new Cookie("accessToken", "nonsense") });
    authFilter.doFilter(request, response, chain);
    verify(response).sendError(401);
    verifyZeroInteractions(chain);
  }

  @Test
  public void testForgedJwt() throws IOException, ServletException, JoseException {
    String forgedToken = new TokenIssuer(UUID.randomUUID().toString().getBytes(Charsets.UTF_8), 60).buildToken(new PrincipalImpl("FOO"), Roles.TRADER).getCompactSerialization();
    when(request.getCookies()).thenReturn(new Cookie[] { new Cookie("accessToken", forgedToken) });
    authFilter.doFilter(request, response, chain);
    verify(response).sendError(401);
    verifyZeroInteractions(chain);
  }

  @Test
  public void testWrongRole() throws IOException, ServletException, JoseException {
    String forgedToken = new TokenIssuer(config.getAuth().getJwt().getSecretBytes(), 60).buildToken(new PrincipalImpl("FOO"), "SOMETHINGELSE").getCompactSerialization();
    when(request.getCookies()).thenReturn(new Cookie[] { new Cookie("accessToken", forgedToken) });
    authFilter.doFilter(request, response, chain);
    verify(response).sendError(401);
    verifyZeroInteractions(chain);
  }

  @Test
  public void testNoRoles() throws IOException, ServletException, JoseException {
    String forgedToken = new TokenIssuer(config.getAuth().getJwt().getSecretBytes(), 60).buildToken(new PrincipalImpl("FOO"), "").getCompactSerialization();
    when(request.getCookies()).thenReturn(new Cookie[] { new Cookie("accessToken", forgedToken) });
    authFilter.doFilter(request, response, chain);
    verify(response).sendError(401);
    verifyZeroInteractions(chain);
  }

  @Test
  public void testValid() throws IOException, ServletException, JoseException {
    when(request.getCookies()).thenReturn(new Cookie[] { new Cookie("accessToken", validJwt().getCompactSerialization()) });
    authFilter.doFilter(request, response, chain);
    verifyZeroInteractions(response);
    verify(chain).doFilter(request, response);
  }

  private JsonWebSignature validJwt() {
    return new TokenIssuer(config.getAuth().getJwt().getSecretBytes(), 60).buildToken(new PrincipalImpl("FOO"), Roles.TRADER);
  }
}