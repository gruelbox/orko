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

package com.gruelbox.orko.job.script;

import org.alfasoftware.morf.upgrade.TableContribution;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.gruelbox.orko.db.EntityContribution;
import com.gruelbox.tools.dropwizard.guice.resources.WebResource;

public class ScriptModule extends AbstractModule {
  @Override
  protected void configure() {
    install(new ScriptJobProcessor.Module());
    Multibinder.newSetBinder(binder(), WebResource.class)
      .addBinding().to(ScriptResource.class);
    Multibinder.newSetBinder(binder(), EntityContribution.class)
      .addBinding().to(ScriptContribution.class);
    Multibinder.newSetBinder(binder(), TableContribution.class)
      .addBinding().to(ScriptContribution.class);
  }
}
