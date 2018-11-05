package com.grahamcrockford.orko.auth.ipwhitelisting;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.grahamcrockford.orko.auth.AuthConfiguration;
import com.warrenstrange.googleauth.GoogleAuthenticator;

/**
 * Only one IP can be whitelisted at a time and requires 2FA.
 */
@Singleton
class IpWhitelisting {

  private static final Logger LOGGER = LoggerFactory.getLogger(IpWhitelisting.class);

  private final Provider<HttpServletRequest> request;
  private final GoogleAuthenticator googleAuthenticator;
  private final AuthConfiguration configuration;
  private final IpWhitelistAccess ipWhitelistAccess;

  @Inject
  IpWhitelisting(Provider<HttpServletRequest> request,
                 GoogleAuthenticator googleAuthenticator,
                 AuthConfiguration configuration,
                 IpWhitelistAccess ipWhitelistAccess) {
    this.request = request;
    this.googleAuthenticator = googleAuthenticator;
    this.configuration = configuration;
    this.ipWhitelistAccess = ipWhitelistAccess;
  }

  public boolean authoriseIp() {
    if (configuration.getIpWhitelisting() != null &&  StringUtils.isEmpty(configuration.getIpWhitelisting().getSecretKey()))
      return true;
    String sourceIp = sourceIp();
    if (!ipWhitelistAccess.exists(sourceIp)) {
      LOGGER.error("Access attempt from [{}] not whitelisted", sourceIp);
      return false;
    }
    return true;
  }

  public boolean whiteListRequestIp(int token) {
    if (configuration.getIpWhitelisting() != null &&  StringUtils.isEmpty(configuration.getIpWhitelisting().getSecretKey()))
      return true;

    String ip = sourceIp();
    if (!googleAuthenticator.authorize(configuration.getIpWhitelisting().getSecretKey(), token)) {
      LOGGER.error("Whitelist attempt failed from: " + ip);
      return false;
    }
    ipWhitelistAccess.add(ip);
    LOGGER.info("Whitelisted ip: " + ip);
    return true;
  }

  public boolean deWhitelistIp() {
    if (!authoriseIp())
      return false;
    ipWhitelistAccess.delete(sourceIp());
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