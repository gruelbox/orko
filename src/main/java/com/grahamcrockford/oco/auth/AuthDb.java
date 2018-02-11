package com.grahamcrockford.oco.auth;

import java.util.Map;
import java.util.Optional;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.grahamcrockford.oco.OcoConfiguration;

/**
 * At the moment, we have a compiled-in authentication DB.  Just a stub really.
 */
@Singleton
class AuthDb {

  private final Map<String, User> users;

  @Inject
  AuthDb(OcoConfiguration configuration) {
    this.users = FluentIterable.from(ImmutableList.of(
        new User(configuration.getUserName(), configuration.getPassword(), Roles.PUBLIC, Roles.TRADER)
        ))
        .uniqueIndex(p -> p.getName().toLowerCase());
  }

  Optional<User> loadUser(String userName) {
    return Optional.ofNullable(users.get(userName.toLowerCase()));
  }
}