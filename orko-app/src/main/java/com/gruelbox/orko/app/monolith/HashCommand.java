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
      .help("An encryption salt. If not provided, a new one will be returned.");
    subparser.addArgument("value")
      .help("The value for which to create a hash");
  }

  @Override
  public void onError(Cli cli, Namespace namespace, Throwable e) {
    cli.getStdErr().println(e.getMessage());
  }
}