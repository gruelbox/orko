package com.grahamcrockford.orko.auth.ipwhitelisting;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.grahamcrockford.orko.auth.AuthConfiguration;
import com.grahamcrockford.orko.auth.Headers;
import com.warrenstrange.googleauth.IGoogleAuthenticator;

/**
 * Only one IP can be whitelisted at a time and requires 2FA.
 */
@Singleton
class IpWhitelisting {

  private static final Logger LOGGER = LoggerFactory.getLogger(IpWhitelisting.class);

  private final Provider<HttpServletRequest> request;
  private final IGoogleAuthenticator googleAuthenticator;
  private final AuthConfiguration configuration;
  private final Provider<IpWhitelistAccess> ipWhitelistAccess;

  @Inject
  IpWhitelisting(Provider<HttpServletRequest> request,
                 IGoogleAuthenticator googleAuthenticator,
                 AuthConfiguration configuration,
                 Provider<IpWhitelistAccess> ipWhitelistAccess) {
    this.request = request;
    this.googleAuthenticator = googleAuthenticator;
    this.configuration = configuration;
    this.ipWhitelistAccess = ipWhitelistAccess;
  }

  public boolean authoriseIp() {
    if (isDisabled())
      return true;
    String sourceIp = sourceIp();
    if (!ipWhitelistAccess.get().exists(sourceIp)) {
      LOGGER.error("Access attempt from [{}] not whitelisted", sourceIp);
      return false;
    }
    return true;
  }

  public boolean whiteListRequestIp(int token) {
    if (isDisabled())
      return true;

    String ip = sourceIp();
    if (!googleAuthenticator.authorize(configuration.getIpWhitelisting().getSecretKey(), token)) {
      LOGGER.error("Whitelist attempt failed from: " + ip);
      return false;
    }
    ipWhitelistAccess.get().add(ip);
    LOGGER.info("Whitelisted ip: " + ip);
    return true;
  }

  public boolean deWhitelistIp() {
    if (isDisabled())
      return false;
    if (!authoriseIp())
      return false;
    ipWhitelistAccess.get().delete(sourceIp());
    return true;
  }

  private String sourceIp() {
    HttpServletRequest req = request.get();
    if (configuration.isProxied()) {
      String header = req.getHeader(Headers.X_FORWARDED_FOR);
      if (StringUtils.isEmpty(header)) {
        throw new IllegalStateException("Configured to assume application is behind a proxy but the forward header has not been provided. "
            + "Headers available: " + Headers.listForRequest(req).toList());
      }
      return header.split(",")[0];
    } else {
      return req.getRemoteAddr();
    }
  }

  private boolean isDisabled() {
    return configuration.getIpWhitelisting() == null || !configuration.getIpWhitelisting().isEnabled();
  }
}