package com.grahamcrockford.oco.web.auth;

import java.security.Principal;
import java.util.List;

import com.google.common.collect.ImmutableList;

public class User implements Principal {

  private final String username;
  private final String password;
  private final List<String> roles;

  public User(String username, String password, String... roles) {
    this.username = username;
    this.password = password;
    this.roles = ImmutableList.copyOf(roles);
  }

  public List<String> getRoles() {
    return roles;
  }

  public boolean isUserInRole(String roleToCheck) {
    return roles.contains(roleToCheck);
  }

  @Override
  public String getName() {
    return username;
  }

  String getPassword() {
    return password;
  }
}