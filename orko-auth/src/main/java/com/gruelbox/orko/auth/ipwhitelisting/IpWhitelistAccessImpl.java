package com.gruelbox.orko.auth.ipwhitelisting;

import static java.time.ZoneOffset.UTC;
import static org.alfasoftware.morf.metadata.SchemaUtils.column;
import static org.alfasoftware.morf.metadata.SchemaUtils.index;
import static org.alfasoftware.morf.metadata.SchemaUtils.table;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.alfasoftware.morf.metadata.DataType;
import org.alfasoftware.morf.metadata.Table;
import org.alfasoftware.morf.upgrade.TableContribution;
import org.alfasoftware.morf.upgrade.UpgradeStep;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.impl.DSL;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.gruelbox.orko.auth.AuthConfiguration;
import com.gruelbox.orko.db.ConnectionSource;

import io.dropwizard.lifecycle.Managed;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

@Singleton
class IpWhitelistAccessImpl implements IpWhitelistAccess, TableContribution, Managed {

  private static final String IP = "ip";
  private static final Field<String> IP_FIELD = DSL.field(IP, String.class);
  
  private static final String EXPIRES = "expires";
  private static final Field<Long> EXPIRES_FIELD = DSL.field(EXPIRES, Long.class);

  private static final String IP_WHITELIST = "IpWhitelist";
  private static final org.jooq.Table<Record> IP_WHITELIST_TABLE = DSL.table(IP_WHITELIST);
  
  private final ConnectionSource connectionSource;
  private final AuthConfiguration authConfiguration;
  
  private final int expiry;
  private final TimeUnit expiryUnits;
  
  private Disposable subscription;
  
  @Inject
  IpWhitelistAccessImpl(ConnectionSource connectionSource, AuthConfiguration authConfiguration) {
    this(connectionSource, authConfiguration, 1, TimeUnit.MINUTES);
  }
  
  @VisibleForTesting
  IpWhitelistAccessImpl(ConnectionSource connectionSource, AuthConfiguration authConfiguration, int expiry, TimeUnit expiryUnits) {
    this.connectionSource = connectionSource;
    this.authConfiguration = authConfiguration;
    this.expiry = expiry;
    this.expiryUnits = expiryUnits;
  }
  
  @Override
  public void start() throws Exception {
    subscription = Observable.interval(expiry, expiryUnits).observeOn(Schedulers.single()).subscribe(x -> cleanup());
  }

  @Override
  public void stop() throws Exception {
    subscription.dispose();
  }

  @VisibleForTesting
  void cleanup() {
    long expiry = LocalDateTime.now().toEpochSecond(UTC);
    connectionSource.runInTransaction(dsl -> dsl.deleteFrom(IP_WHITELIST_TABLE).where(EXPIRES_FIELD.lessOrEqual(expiry)).execute());
  }
  
  @Override
  public synchronized void add(String ip) {
    if (authConfiguration.getIpWhitelisting() == null)
      return;
    Preconditions.checkNotNull(ip);
    connectionSource.runInTransaction(dsl -> {
      if (!existsInner(ip, dsl)) {
        dsl.insertInto(IP_WHITELIST_TABLE).values(ip, newExpiryDate()).execute();
      }
    });
  }

  private long newExpiryDate() {
    return LocalDateTime.now().plusSeconds(authConfiguration.getIpWhitelisting().getWhitelistExpirySeconds()).toEpochSecond(UTC);
  }

  @Override
  public synchronized void delete(String ip) {
    connectionSource.runInTransaction(dsl -> dsl.deleteFrom(IP_WHITELIST_TABLE).where(IP_FIELD.eq(ip)).execute());
  }

  @Override
  public synchronized boolean exists(String ip) {
    return connectionSource.getInTransaction(dsl -> existsInner(ip, dsl));
  }
  
  private boolean existsInner(String ip, DSLContext dsl) {
    return !dsl.select(DSL.inline(1)).from(IP_WHITELIST_TABLE).where(IP_FIELD.eq(ip)).fetch().isEmpty();
  }

  @Override
  public Collection<Table> tables() {
    return ImmutableList.of(
      table(IP_WHITELIST)
        .columns(
          column(IP, DataType.STRING, 45).primaryKey(),
          column(EXPIRES, DataType.BIG_INTEGER)
        )
        .indexes(
          index(IP_WHITELIST + "_1").columns("expires")
        )
    );
  }

  @Override
  public Collection<Class<? extends UpgradeStep>> schemaUpgradeClassses() {
    return ImmutableList.of();
  }
}