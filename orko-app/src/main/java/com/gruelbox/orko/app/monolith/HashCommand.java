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

import com.gruelbox.orko.auth.Hasher;

import io.dropwizard.cli.Cli;
import io.dropwizard.cli.Command;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

class HashCommand extends Command {

  HashCommand() {
    super("hash", "Hashes the specified value using a provided salt");
  }

  @Override
  public void run(Bootstrap<?> bootstrap, Namespace namespace) throws Exception {
    Hasher hasher = new Hasher();
    String salt = namespace.getString("salt");
    if (salt == null)
      salt = hasher.salt();
    System.out.println("Salt used: " + salt);
    System.out.println("Hashed result: " + hasher.hashWithString(namespace.getString("value"), salt));
  }

  @Override
  public void configure(Subparser subparser) {
    subparser.addArgument("--salt", "-s")
      .help("An encryption salt. If not provided, a new one will be used and returned.");
    subparser.addArgument("value")
      .required(true)
      .help("The value for which to create a hash.");
  }

  @Override
  public void onError(Cli cli, Namespace namespace, Throwable e) {
    cli.getStdErr().println(e.getMessage());
  }
}