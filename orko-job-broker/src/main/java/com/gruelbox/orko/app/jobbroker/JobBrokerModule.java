/**
 * Orko
 * Copyright © 2018-2019 Graham Crockford
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.gruelbox.orko.app.jobbroker;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.gruelbox.orko.OrkoConfiguration;
import com.gruelbox.orko.db.DbModule;
import com.gruelbox.orko.job.JobsModule;
import com.gruelbox.orko.jobrun.InProcessJobSubmitter;
import com.gruelbox.orko.jobrun.JobRunModule;
import com.gruelbox.orko.jobrun.JobSubmitter;
import com.gruelbox.orko.jobrun.spi.JobRunConfiguration;
import com.gruelbox.orko.notification.NotificationModule;
import com.gruelbox.orko.wiring.WiringModule;

/**
 * Top level bindings.
 */
class JobBrokerModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(JobSubmitter.class).to(InProcessJobSubmitter.class);
    install(new WiringModule());
    install(new DbModule());
    install(new JobRunModule());
    install(new JobsModule());
    install(new NotificationModule());
  }

  @Provides
  @Singleton
  JobRunConfiguration jobRunConfiguration(OrkoConfiguration orkoConfiguration) {
    JobRunConfiguration jobRunConfiguration = new JobRunConfiguration();
    jobRunConfiguration.setDatabaseLockSeconds(orkoConfiguration.getDatabase().getLockSeconds());
    jobRunConfiguration.setGuardianLoopSeconds(orkoConfiguration.getLoopSeconds());
    return jobRunConfiguration;
  }
}