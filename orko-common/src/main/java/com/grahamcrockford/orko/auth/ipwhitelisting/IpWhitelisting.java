package com.grahamcrockford.orko.auth.ipwhitelisting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.grahamcrockford.orko.auth.AuthConfiguration;
import com.grahamcrockford.orko.auth.RequestUtils;
import com.warrenstrange.googleauth.IGoogleAuthenticator;

/**
 * Only one IP can be whitelisted at a time and requires 2FA.
 */
@Singleton
class IpWhitelisting {

  private static final Logger LOGGER = LoggerFactory.getLogger(IpWhitelisting.class);

  private final Provider<RequestUtils> requestUtils;
  private final IGoogleAuthenticator googleAuthenticator;
  private final AuthConfiguration configuration;
  private final Provider<IpWhitelistAccess> ipWhitelistAccess;

  @Inject
  IpWhitelisting(Provider<RequestUtils> requestUtils,
                 IGoogleAuthenticator googleAuthenticator,
                 AuthConfiguration configuration,
                 Provider<IpWhitelistAccess> ipWhitelistAccess) {
    this.requestUtils = requestUtils;
    this.googleAuthenticator = googleAuthenticator;
    this.configuration = configuration;
    this.ipWhitelistAccess = ipWhitelistAccess;
  }

  public boolean authoriseIp() {
    if (isDisabled())
      return true;
    String sourceIp = requestUtils.get().sourceIp();
    if (!ipWhitelistAccess.get().exists(sourceIp)) {
      LOGGER.error("Access attempt from [{}] not whitelisted", sourceIp);
      return false;
    }
    return true;
  }

  public boolean whiteListRequestIp(int token) {
    if (isDisabled())
      return true;
    String ip = requestUtils.get().sourceIp();
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
    
    ipWhitelistAccess.get().delete(requestUtils.get().sourceIp());
    return true;
  }

  private boolean isDisabled() {
    return configuration.getIpWhitelisting() == null || !configuration.getIpWhitelisting().isEnabled();
  }
}