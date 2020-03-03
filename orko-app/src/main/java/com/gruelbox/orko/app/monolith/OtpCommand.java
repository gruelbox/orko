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

import com.gruelbox.orko.auth.GenerateSecretKey;
import io.dropwizard.cli.Cli;
import io.dropwizard.cli.Command;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

class OtpCommand extends Command {

  private static final String NOCHECK = "--nocheck";

  OtpCommand() {
    super("otp", "Generates a new 2FA key for Google Authenticator");
  }

  @Override
  public void run(Bootstrap<?> bootstrap, Namespace namespace) throws Exception {
    if (Boolean.TRUE.equals(namespace.getBoolean("nocheck"))) {
      GenerateSecretKey.main(NOCHECK);
    } else {
      GenerateSecretKey.main();
    }
  }

  @Override
  public void configure(Subparser subparser) {
    subparser
        .addArgument(NOCHECK, "-n")
        .nargs("?")
        .setConst(true)
        .help("Skips manual checking, just outputting the new key.");
  }

  @Override
  public void onError(Cli cli, Namespace namespace, Throwable e) {
    cli.getStdErr().println(e.getMessage());
  }
}
