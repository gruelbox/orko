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
import org.hibernate.SessionFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.gruelbox.orko.auth.AuthConfiguration;
import com.gruelbox.orko.db.EntityContribution;
import com.gruelbox.orko.db.Transactionally;
import com.gruelbox.orko.util.SafelyDispose;

import io.dropwizard.lifecycle.Managed;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

@Singleton
class IpWhitelistAccessImpl implements IpWhitelistAccess, TableContribution, EntityContribution, Managed {

  private final AuthConfiguration authConfiguration;
  private final Provider<SessionFactory> sessionFactory;
  private final Transactionally transactionally;
  private final int expiry;
  private final TimeUnit expiryUnits;

  private Disposable subscription;

  @Inject
  IpWhitelistAccessImpl(Provider<SessionFactory> sessionFactory, Transactionally transactionally, AuthConfiguration authConfiguration) {
    this(sessionFactory, transactionally, authConfiguration, 1, TimeUnit.MINUTES);
  }

  @VisibleForTesting
  IpWhitelistAccessImpl(Provider<SessionFactory> sessionFactory,
                        Transactionally transactionally,
                        AuthConfiguration authConfiguration,
                        int expiry, TimeUnit expiryUnits) {
    this.sessionFactory = sessionFactory;
    this.transactionally = transactionally;
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
    SafelyDispose.of(subscription);
  }

  @VisibleForTesting
  void cleanup() {
    transactionally.run(() ->
      sessionFactory.get().getCurrentSession()
          .createQuery("delete from " + IpWhitelist.TABLE_NAME + " where expires <= :expires")
          .setParameter("expires", LocalDateTime.now().toEpochSecond(UTC))
          .executeUpdate()
    );
  }

  @Override
  public synchronized void add(String ip) {
    if (authConfiguration.getIpWhitelisting() == null)
      return;
    Preconditions.checkNotNull(ip);
    sessionFactory.get().getCurrentSession().merge(new IpWhitelist(ip, newExpiryDate()));
  }

  private long newExpiryDate() {
    return LocalDateTime.now().plusSeconds(authConfiguration.getIpWhitelisting().getWhitelistExpirySeconds()).toEpochSecond(UTC);
  }

  @Override
  public synchronized void delete(String ip) {
    sessionFactory.get().getCurrentSession()
        .createQuery("delete from " +  IpWhitelist.TABLE_NAME + " where id = :id")
        .setParameter("id", ip)
        .executeUpdate();
  }

  @Override
  public synchronized boolean exists(String ip) {
    return sessionFactory.get().getCurrentSession().get(IpWhitelist.class, ip) != null;
  }

  @Override
  public Collection<Table> tables() {
    return ImmutableList.of(
      table(IpWhitelist.TABLE_NAME)
        .columns(
          column(IpWhitelist.IP, DataType.STRING, 45).primaryKey(),
          column(IpWhitelist.EXPIRES, DataType.BIG_INTEGER)
        )
        .indexes(
          index(IpWhitelist.TABLE_NAME + "_1").columns("expires")
        )
    );
  }

  @Override
  public Collection<Class<? extends UpgradeStep>> schemaUpgradeClassses() {
    return ImmutableList.of();
  }

  @Override
  public Iterable<Class<?>> getEntities() {
    return ImmutableList.of(IpWhitelist.class);
  }
}