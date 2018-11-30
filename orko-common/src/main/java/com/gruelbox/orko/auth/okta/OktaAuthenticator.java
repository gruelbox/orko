package com.gruelbox.orko.auth.okta;

import com.gruelbox.orko.auth.AuthenticatedUser;

import io.dropwizard.auth.Authenticator;

interface OktaAuthenticator extends Authenticator<String, AuthenticatedUser> {
}
