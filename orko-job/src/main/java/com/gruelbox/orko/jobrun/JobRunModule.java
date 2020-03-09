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
package com.gruelbox.orko.jobrun;

import com.google.common.util.concurrent.Service;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.gruelbox.orko.db.DbModule;
import com.gruelbox.orko.wiring.WiringModule;
import com.gruelbox.tools.dropwizard.guice.hibernate.EntityContribution;
import com.gruelbox.tools.dropwizard.guice.resources.WebResource;
import io.dropwizard.lifecycle.Managed;
import org.alfasoftware.morf.upgrade.TableContribution;

public class JobRunModule extends AbstractModule {
  @Override
  protected void configure() {
    install(new DbModule());
    install(new WiringModule());
    Multibinder<TableContribution> tableContributions =
        Multibinder.newSetBinder(binder(), TableContribution.class);
    tableContributions.addBinding().to(JobRecordContribution.class);
    tableContributions.addBinding().to(JobLockContribution.class);

    Multibinder<EntityContribution> entityContributions =
        Multibinder.newSetBinder(binder(), EntityContribution.class);
    entityContributions.addBinding().to(JobRecordContribution.class);

    Multibinder.newSetBinder(binder(), Managed.class).addBinding().to(JobLockerImpl.class);
    Multibinder.newSetBinder(binder(), Service.class).addBinding().to(GuardianLoop.class);
    Multibinder.newSetBinder(binder(), WebResource.class).addBinding().to(JobResource.class);
  }
}
