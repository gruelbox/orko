package com.grahamcrockford.orko.db;

import java.util.concurrent.ConcurrentMap;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.DBMaker.Maker;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.grahamcrockford.orko.auth.AuthConfiguration;
import com.grahamcrockford.orko.auth.IpWhitelistAccess;

@Singleton
class MapDbIpWhitelistAccess implements IpWhitelistAccess {

  private static final Logger LOGGER = LoggerFactory.getLogger(MapDbIpWhitelistAccess.class);

  private final Maker dbMaker;
  private final AuthConfiguration authConfiguration;

  @Inject
  MapDbIpWhitelistAccess(DBMaker.Maker dbMaker, AuthConfiguration authConfiguration) {
    this.dbMaker = dbMaker;
    this.authConfiguration = authConfiguration;
  }

  @Override
  public synchronized void add(String ip) {
    Preconditions.checkNotNull(ip);
    try (DB db = dbMaker.make()) {
      ips(db).put(ip, System.currentTimeMillis());
      db.commit();
    }
  }

  @Override
  public synchronized void delete(String ip) {
    try (DB db = dbMaker.make()) {
      ips(db).remove(ip);
      db.commit();
    }
  }

  @Override
  public synchronized boolean exists(String ip) {
    try (DB db = dbMaker.make()) {
      ConcurrentMap<String, Long> ips = ips(db);
      Long createdTime = ips.get(ip);
      if (createdTime == null) {
        LOGGER.warn("No whitelisting found for " + ip);
        return false;
      }
      if (System.currentTimeMillis() > createdTime + (authConfiguration.whitelistExpirySeconds * 1000)) {
        LOGGER.warn("Expiring IP whitelisting for " + ip);
        ips.remove(ip);
        db.commit();
        return false;
      }
      return true;
    }
  }

  private ConcurrentMap<String, Long> ips(DB db) {
    return db.hashMap("ipwl", Serializer.STRING, Serializer.LONG).createOrOpen();
  }
}