package com.grahamcrockford.oco.auth;

import java.util.Map;
import java.util.Optional;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.inject.Singleton;

/**
 * At the moment, we have a compiled-in authentication DB.  Just a stub really.
 */
@Singleton
class AuthDb {

  private static final Map<String, User> USERS = FluentIterable.from(ImmutableList.of(
      new User("admin", "cherished at birth but abandoned as child", Roles.PUBLIC, Roles.TRADER),
      new User("muppet", "do the muppet dance", Roles.PUBLIC)
    ))
    .uniqueIndex(p -> p.getName().toLowerCase());

  Optional<User> loadUser(String userName) {
    return Optional.ofNullable(USERS.get(userName.toLowerCase()));
  }
}