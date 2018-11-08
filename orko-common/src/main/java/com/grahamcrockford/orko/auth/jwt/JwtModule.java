package com.grahamcrockford.orko.auth.jwt;

import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.keys.HmacKey;

import com.google.common.base.Preconditions;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import com.grahamcrockford.orko.auth.AuthConfiguration;
import com.grahamcrockford.orko.wiring.WebResource;

public class JwtModule extends AbstractModule {

  private final AuthConfiguration auth;

  public JwtModule(AuthConfiguration auth) {
    this.auth = auth;
  }

  @Override
  protected void configure() {
    if (auth.getJwt() != null && auth.getJwt().isEnabled()) {
      Multibinder.newSetBinder(binder(), WebResource.class).addBinding().to(LoginResource.class);
    }
  }

  @Singleton
  @Provides
  JwtConsumer jwtConsumer(AuthConfiguration authConfiguration) {
    Preconditions.checkNotNull(authConfiguration.getJwt());
    return new JwtConsumerBuilder()
      .setAllowedClockSkewInSeconds(30) // allow some leeway in validating time based claims to account for clock skew
      .setRequireExpirationTime() // the JWT must have an expiration time
      .setRequireSubject() // the JWT must have a subject claim
      .setVerificationKey(new HmacKey(authConfiguration.getJwt().getSecretBytes())) // verify the signature with the public key
      .setRelaxVerificationKeyValidation() // relaxes key length requirement
      .build();
  }
}