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
package com.gruelbox.orko.subscription;

import static com.gruelbox.orko.subscription.Subscription.TABLE_NAME;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.gruelbox.orko.spi.TickerSpec;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Subscription persistence.
 *
 * @author Graham Crockford
 */
@Singleton
class SubscriptionAccess {

  private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionAccess.class);

  private final Provider<SessionFactory> sessionFactory;

  @Inject
  SubscriptionAccess(Provider<SessionFactory> sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  void add(TickerSpec spec) {
    LOGGER.info("Adding permanent subscription to {}", spec);
    Subscription sub = session().get(Subscription.class, spec.key());
    if (sub == null) {
      session().merge(new Subscription(spec, null));
    }
  }

  void remove(TickerSpec spec) {
    LOGGER.info("Removing permanent subscription to {}", spec);
    Subscription subscription = session().get(Subscription.class, spec.key());
    if (subscription != null) session().delete(subscription);
  }

  Set<TickerSpec> all() {
    List<Subscription> results =
        session().createQuery("from " + TABLE_NAME, Subscription.class).list();
    return FluentIterable.from(results).transform(Subscription::getTicker).toSet();
  }

  void setReferencePrice(TickerSpec tickerSpec, BigDecimal price) {
    session().merge(new Subscription(tickerSpec, price));
  }

  Map<TickerSpec, BigDecimal> getReferencePrices() {
    List<Subscription> results =
        session().createQuery("from " + TABLE_NAME, Subscription.class).list();
    Builder<TickerSpec, BigDecimal> builder = ImmutableMap.builder();
    results.stream()
        .filter(r -> r.getReferencePrice() != null)
        .forEach(r -> builder.put(r.getTicker(), r.getReferencePrice()));
    return builder.build();
  }

  private Session session() {
    return sessionFactory.get().getCurrentSession();
  }
}
