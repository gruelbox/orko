package com.grahamcrockford.oco.auth;

import io.dropwizard.auth.Authenticator;

interface OrkoAuthenticator extends Authenticator<String, AccessTokenPrincipal> {

}
