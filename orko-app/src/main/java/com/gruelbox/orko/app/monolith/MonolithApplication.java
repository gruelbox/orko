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

package com.gruelbox.orko.app.monolith;

import com.google.inject.Module;
import com.gruelbox.orko.OrkoConfiguration;
import com.gruelbox.orko.WebHostApplication;

import io.dropwizard.setup.Bootstrap;

public class MonolithApplication extends WebHostApplication {

  public static void main(final String... args) throws Exception {
    new MonolithApplication().run(args);
  }

  @Override
  public String getName() {
    return "Orko all-in-one application";
  }

  @Override
  protected Module createApplicationModule() {
    return new MonolithModule();
  }

  @Override
  protected void addDefaultCommands(Bootstrap<OrkoConfiguration> bootstrap) {
    super.addDefaultCommands(bootstrap);
    bootstrap.addCommand(new OtpCommand());
    bootstrap.addCommand(new HashCommand());
    bootstrap.addCommand(new SaltCommand());
    bootstrap.addCommand(new DbInitCommand());
  }
}