package com.grahamcrockford.orko.auth.spi;

import io.dropwizard.auth.Authenticator;

public interface OrkoAuthenticator extends Authenticator<String, AccessTokenPrincipal> {

}
