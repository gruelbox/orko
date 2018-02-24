package com.grahamcrockford.oco.auth;

import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.warrenstrange.googleauth.GoogleAuthenticator;

/**
 * Only one IP can be whitelisted at a time and requires 2FA.
 */
@Singleton
public class IpWhitelisting {

  private static final Logger LOGGER = LoggerFactory.getLogger(IpWhitelisting.class);

  private final Provider<HttpServletRequest> request;
  private final GoogleAuthenticator googleAuthenticator;
  private final AuthConfiguration configuration;
  private final AtomicReference<String> whiteListedIp = new AtomicReference<String>(null);

  @Inject
  IpWhitelisting(Provider<HttpServletRequest> request, GoogleAuthenticator googleAuthenticator, AuthConfiguration configuration) {
    this.request = request;
    this.googleAuthenticator = googleAuthenticator;
    this.configuration = configuration;
  }

  public boolean authoriseIp() {
    return configuration.getSecretKey() == null || sourceIp().equals(whiteListedIp.get());
  }

  public boolean whiteListRequestIp(int token) {
    if (configuration.getSecretKey() == null)
      return true;

    String ip = sourceIp();
    if (!googleAuthenticator.authorize(configuration.getSecretKey(), token)) {
      LOGGER.error("Whitelist attempt failed from: " + ip);
      return false;
    }
    whiteListedIp.set(ip);
    LOGGER.info("Whitelisted ip: " + ip);
    return true;
  }

  private String sourceIp() {
    HttpServletRequest req = request.get();
    if (configuration.isProxied()) {
      return req.getHeader("X-Forwarded-For").split(",")[0];
    } else {
      return req.getRemoteAddr();
    }
  }
}