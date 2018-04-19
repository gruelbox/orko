package com.grahamcrockford.oco.auth;

import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.Authorizer;

interface AuthenticatorAuthoriser extends Authenticator<String, AccessTokenPrincipal>, Authorizer<AccessTokenPrincipal>  {

}
