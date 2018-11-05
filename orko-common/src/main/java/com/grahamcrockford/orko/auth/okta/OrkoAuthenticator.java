package com.grahamcrockford.orko.auth.okta;

import io.dropwizard.auth.Authenticator;

interface OrkoAuthenticator extends Authenticator<String, AccessTokenPrincipal> {

}
