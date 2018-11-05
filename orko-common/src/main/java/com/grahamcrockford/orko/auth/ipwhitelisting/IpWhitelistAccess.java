package com.grahamcrockford.orko.auth.ipwhitelisting;

public interface IpWhitelistAccess {

  void add(String ip);

  void delete(String ip);

  boolean exists(String ip);

}