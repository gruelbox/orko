package com.gruelbox.orko.auth.ipwhitelisting;

import com.google.inject.ImplementedBy;

@ImplementedBy(IpWhitelistAccessImpl.class)
public interface IpWhitelistAccess {

  void add(String ip);

  void delete(String ip);

  boolean exists(String ip);

}