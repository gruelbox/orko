package com.grahamcrockford.oco.auth;

import io.dropwizard.auth.Authenticator;

interface OcoAuthenticator extends Authenticator<String, AccessTokenPrincipal> {

}
