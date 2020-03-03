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

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.gruelbox.orko.db.DbModule;
import com.gruelbox.tools.dropwizard.guice.hibernate.EntityContribution;
import com.gruelbox.tools.dropwizard.guice.resources.WebResource;
import io.dropwizard.lifecycle.Managed;
import org.alfasoftware.morf.upgrade.TableContribution;

public class SubscriptionModule extends AbstractModule {
  @Override
  protected void configure() {
    install(new DbModule());
    Multibinder.newSetBinder(binder(), WebResource.class)
        .addBinding()
        .to(SubscriptionResource.class);
    Multibinder.newSetBinder(binder(), Managed.class).addBinding().to(SubscriptionManager.class);
    Multibinder.newSetBinder(binder(), TableContribution.class)
        .addBinding()
        .to(SubscriptionContribution.class);
    Multibinder.newSetBinder(binder(), EntityContribution.class)
        .addBinding()
        .to(SubscriptionContribution.class);
  }
}
