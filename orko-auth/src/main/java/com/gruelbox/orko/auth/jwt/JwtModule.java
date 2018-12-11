package com.gruelbox.orko.auth.jwt;

import java.util.Optional;

import javax.annotation.Nullable;

import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.jwt.consumer.JwtContext;
import org.jose4j.keys.HmacKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Named;
import com.google.inject.servlet.RequestScoped;
import com.gruelbox.orko.auth.AuthConfiguration;
import com.gruelbox.orko.auth.AuthModule;
import com.gruelbox.orko.auth.jwt.login.LoginResource;
import com.gruelbox.tools.dropwizard.guice.resources.WebResource;

public class JwtModule extends AbstractModule {

  private final AuthConfiguration auth;

  private static final Logger LOGGER = LoggerFactory.getLogger(JwtModule.class);

  public JwtModule(AuthConfiguration auth) {
    this.auth = auth;
  }

  @Override
  protected void configure() {
    if (auth.getJwt() != null && auth.getJwt().isEnabled()) {
      Multibinder.newSetBinder(binder(), WebResource.class).addBinding().to(LoginResource.class);
    }
  }

  @Nullable
  @Provides
  JwtConfiguration config(AuthConfiguration authConfiguration) {
    return authConfiguration.getJwt();
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

  @RequestScoped
  @Provides
  Optional<JwtContext> jwtContext(JwtConsumer jwtConsumer, @Named(AuthModule.ACCESS_TOKEN_KEY) Optional<String> accessToken) {
    if (!accessToken.isPresent())
      return Optional.empty();
    try {
      return Optional.of(jwtConsumer.process(accessToken.get()));
    } catch (InvalidJwtException e) {
      LOGGER.warn("Invalid JWT (" + e.getMessage() + ")");
      return Optional.empty();
    }
  }
}