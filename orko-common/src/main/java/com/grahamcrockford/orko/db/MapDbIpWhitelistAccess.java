package com.grahamcrockford.orko.db;

import java.util.concurrent.TimeUnit;

import org.mapdb.DB;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.grahamcrockford.orko.auth.AuthConfiguration;
import com.grahamcrockford.orko.auth.IpWhitelistAccess;

@Singleton
class MapDbIpWhitelistAccess implements IpWhitelistAccess {

  private final HTreeMap<String, Long> ips;
  private final DB db;

  @Inject
  MapDbIpWhitelistAccess(MapDbMakerFactory dbMakerFactory, AuthConfiguration authConfiguration) {
    this.db = dbMakerFactory.create("ipwl").make();
    this.ips = db.hashMap("ipwl", Serializer.STRING, Serializer.LONG)
        .expireAfterUpdate(authConfiguration.getWhitelistExpirySeconds(), TimeUnit.SECONDS)
        .createOrOpen();
  }

  @Override
  public synchronized void add(String ip) {
    Preconditions.checkNotNull(ip);
    ips.put(ip, System.currentTimeMillis());
    db.commit();
  }

  @Override
  public synchronized void delete(String ip) {
    ips.remove(ip);
    db.commit();
  }

  @Override
  public synchronized boolean exists(String ip) {
    return ips.containsKey(ip);
  }
}