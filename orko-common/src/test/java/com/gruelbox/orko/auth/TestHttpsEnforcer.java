package com.gruelbox.orko.auth;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.gruelbox.orko.auth.HttpsEnforcementModule.HttpsEnforcer;

public class TestHttpsEnforcer {

  private static final String CONTENT_SECURITY_HEADER = "max-age=63072000; includeSubDomains; preload";

  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;
  @Mock private FilterChain filterChain;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testUnproxiedPassthrough() throws IOException, ServletException {
    HttpsEnforcer httpsEnforcer = new HttpsEnforcementModule.HttpsEnforcer(false);
    when(request.isSecure()).thenReturn(true);

    httpsEnforcer.doFilter(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verify(response).addHeader(Headers.STRICT_CONTENT_SECURITY, CONTENT_SECURITY_HEADER);
    verifyNoMoreInteractions(response);
  }

  @Test
  public void testUnproxiedRedirectSimple() throws IOException, ServletException {
    HttpsEnforcer httpsEnforcer = new HttpsEnforcementModule.HttpsEnforcer(false);
    when(request.isSecure()).thenReturn(false);
    when(request.getProtocol()).thenReturn("http");
    when(request.getServerName()).thenReturn("foo.com");
    when(request.getRequestURI()).thenReturn("/here/and/there");

    httpsEnforcer.doFilter(request, response, filterChain);

    verify(response).sendRedirect("https://foo.com/here/and/there");
    verifyNoMoreInteractions(response, filterChain);
  }

  @Test(expected=IllegalStateException.class)
  public void testUnproxiedRedirectButIsHttps() throws IOException, ServletException {
    HttpsEnforcer httpsEnforcer = new HttpsEnforcementModule.HttpsEnforcer(false);
    when(request.isSecure()).thenReturn(false);
    when(request.getProtocol()).thenReturn("https");
    when(request.getServerName()).thenReturn("foo.com");
    when(request.getRequestURI()).thenReturn("/here/and/there");

    httpsEnforcer.doFilter(request, response, filterChain);
  }

  @Test
  public void testProxiedPassthrough() throws IOException, ServletException {
    HttpsEnforcer httpsEnforcer = new HttpsEnforcementModule.HttpsEnforcer(true);
    when(request.getHeader(Headers.X_FORWARDED_PROTO)).thenReturn("https");

    httpsEnforcer.doFilter(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verify(response).addHeader(Headers.STRICT_CONTENT_SECURITY, CONTENT_SECURITY_HEADER);
    verifyNoMoreInteractions(response);
  }

  @Test
  public void testProxiedRedirectSimple() throws IOException, ServletException {
    HttpsEnforcer httpsEnforcer = new HttpsEnforcementModule.HttpsEnforcer(true);
    when(request.getHeader(Headers.X_FORWARDED_PROTO)).thenReturn("http");
    when(request.getServerName()).thenReturn("foo.com");
    when(request.getRequestURI()).thenReturn("/here/and/there");

    httpsEnforcer.doFilter(request, response, filterChain);

    verify(response).sendRedirect("https://foo.com/here/and/there");
    verifyNoMoreInteractions(response, filterChain);
  }
  
  @Test
  public void testProxiedRedirectPort80() throws IOException, ServletException {
    HttpsEnforcer httpsEnforcer = new HttpsEnforcementModule.HttpsEnforcer(true);
    when(request.getHeader(Headers.X_FORWARDED_PROTO)).thenReturn("http");
    when(request.getServerName()).thenReturn("foo.com");
    when(request.getRequestURI()).thenReturn("/here/and/there");
    when(request.getServerPort()).thenReturn(80);

    httpsEnforcer.doFilter(request, response, filterChain);

    verify(response).sendRedirect("https://foo.com/here/and/there");
    verifyNoMoreInteractions(response, filterChain);
  }


  @Test
  public void testProxiedRedirectComplex() throws IOException, ServletException {
    HttpsEnforcer httpsEnforcer = new HttpsEnforcementModule.HttpsEnforcer(true);
    when(request.getHeader(Headers.X_FORWARDED_PROTO)).thenReturn("http");
    when(request.getServerName()).thenReturn("foo.com");
    when(request.getServerPort()).thenReturn(5634);
    when(request.getRequestURI()).thenReturn("/here/and/there");
    when(request.getQueryString()).thenReturn("do=this&do=that");

    httpsEnforcer.doFilter(request, response, filterChain);

    verify(response).sendRedirect("https://foo.com:5634/here/and/there?do=this&do=that");
    verifyNoMoreInteractions(response, filterChain);
  }

  @Test(expected=IllegalArgumentException.class)
  public void testProxiedRedirectWithSplitAttack() throws IOException, ServletException {
    HttpsEnforcer httpsEnforcer = new HttpsEnforcementModule.HttpsEnforcer(true);
    when(request.getHeader(Headers.X_FORWARDED_PROTO)).thenReturn("http");
    when(request.getServerName()).thenReturn("foo.com");
    when(request.getRequestURI()).thenReturn("/here/and/there");
    when(request.getQueryString()).thenReturn("do=this&do=that\nAHA I HAVE YOU NOW");

    httpsEnforcer.doFilter(request, response, filterChain);
  }

  @Test(expected=IllegalStateException.class)
  public void testProxiedMissingHeader() throws IOException, ServletException {
    HttpsEnforcer httpsEnforcer = new HttpsEnforcementModule.HttpsEnforcer(true);
    when(request.getHeaderNames()).thenReturn(new Enumeration<String>() {
      @Override
      public String nextElement() {
        return null;
      }
      @Override
      public boolean hasMoreElements() {
        return false;
      }
    });

    httpsEnforcer.doFilter(request, response, filterChain);
  }
}
