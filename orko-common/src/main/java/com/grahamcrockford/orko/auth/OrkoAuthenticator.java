package com.grahamcrockford.orko.auth;

import io.dropwizard.auth.Authenticator;

interface OrkoAuthenticator extends Authenticator<String, AccessTokenPrincipal> {

}
