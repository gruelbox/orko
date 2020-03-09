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
package com.gruelbox.orko.app.monolith;

import com.gruelbox.orko.db.DbDump;
import io.dropwizard.cli.Cli;
import io.dropwizard.cli.ConfiguredCommand;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.alfasoftware.morf.jdbc.ConnectionResources;

/** Command line command to restore a database dump */
class DbInitCommand extends ConfiguredCommand<MonolithConfiguration> {

  DbInitCommand() {
    super(
        "dbinit",
        "Imports a database snapshot taken using the /api/db.zip endpoint to the configured DB");
  }

  @Override
  protected void run(
      Bootstrap<MonolithConfiguration> bootstrap,
      Namespace namespace,
      MonolithConfiguration configuration)
      throws Exception {
    ConnectionResources connectionResources = configuration.getDatabase().toConnectionResources();
    new DbDump(connectionResources).restore(namespace.getString("dbfile"));
  }

  @Override
  public void configure(Subparser subparser) {
    super.configure(subparser);
    subparser.addArgument("dbfile").required(true).help("The database dump filename to load");
  }

  @Override
  public void onError(Cli cli, Namespace namespace, Throwable e) {
    cli.getStdErr().println(e.getMessage());
  }
}
