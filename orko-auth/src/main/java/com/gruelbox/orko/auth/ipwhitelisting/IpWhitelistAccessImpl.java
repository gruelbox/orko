/**
 * Orko - Copyright © 2018-2019 Graham Crockford
 *
 * <p>This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * <p>You should have received a copy of the GNU Affero General Public License along with this
 * program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.gruelbox.orko.auth.ipwhitelisting;

import static java.time.ZoneOffset.UTC;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.gruelbox.orko.auth.AuthConfiguration;
import com.gruelbox.orko.db.Transactionally;
import com.gruelbox.orko.util.SafelyDispose;
import io.dropwizard.lifecycle.Managed;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

@Singleton
class IpWhitelistAccessImpl implements IpWhitelistAccess, Managed {

  private final AuthConfiguration authConfiguration;
  private final Provider<SessionFactory> sessionFactory;
  private final Transactionally transactionally;
  private final int expiry;
  private final TimeUnit expiryUnits;

  private Disposable subscription;

  @Inject
  IpWhitelistAccessImpl(
      Provider<SessionFactory> sessionFactory,
      Transactionally transactionally,
      AuthConfiguration authConfiguration) {
    this(sessionFactory, transactionally, authConfiguration, 1, TimeUnit.MINUTES);
  }

  @VisibleForTesting
  IpWhitelistAccessImpl(
      Provider<SessionFactory> sessionFactory,
      Transactionally transactionally,
      AuthConfiguration authConfiguration,
      int expiry,
      TimeUnit expiryUnits) {
    this.sessionFactory = sessionFactory;
    this.transactionally = transactionally;
    this.authConfiguration = authConfiguration;
    this.expiry = expiry;
    this.expiryUnits = expiryUnits;
  }

  @Override
  public void start() throws Exception {
    subscription =
        Observable.interval(expiry, expiryUnits).subscribe(x -> transactionally.run(this::cleanup));
  }

  @Override
  public void stop() throws Exception {
    SafelyDispose.of(subscription);
  }

  @VisibleForTesting
  void cleanup() {
    session().clear();
    session()
        .createQuery("delete from " + IpWhitelist.TABLE_NAME + " where expires <= :expires")
        .setParameter("expires", LocalDateTime.now().toEpochSecond(UTC))
        .executeUpdate();
    session().flush();
  }

  @Override
  public synchronized void add(String ip) {
    if (authConfiguration.getIpWhitelisting() == null) return;
    Preconditions.checkNotNull(ip);
    session().merge(new IpWhitelist(ip, newExpiryDate()));
  }

  private long newExpiryDate() {
    return LocalDateTime.now()
        .plusSeconds(authConfiguration.getIpWhitelisting().getWhitelistExpirySeconds())
        .toEpochSecond(UTC);
  }

  @Override
  public synchronized void delete(String ip) {
    IpWhitelist entry = session().get(IpWhitelist.class, ip);
    if (entry != null) session().delete(entry);
  }

  @Override
  public synchronized boolean exists(String ip) {
    return session().get(IpWhitelist.class, ip) != null;
  }

  private Session session() {
    return sessionFactory.get().getCurrentSession();
  }
}
