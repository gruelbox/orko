package com.grahamcrockford.orko.auth.okta;

import com.grahamcrockford.orko.auth.AuthenticatedUser;

import io.dropwizard.auth.Authenticator;

interface OktaAuthenticator extends Authenticator<String, AuthenticatedUser> {
}
