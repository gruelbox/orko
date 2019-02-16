package com.gruelbox.orko.app.monolith;

import com.gruelbox.orko.auth.Hasher;

import io.dropwizard.cli.Cli;
import io.dropwizard.cli.Command;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

class SaltCommand extends Command {

  SaltCommand() {
    super("salt", "Generates a new pseudorandom salt for use with password hashing");
  }

  @Override
  public void configure(Subparser subparser) {
    // No-op
  }

  @Override
  public void run(Bootstrap<?> bootstrap, Namespace namespace) throws Exception {
    System.out.println("Salt: " + new Hasher().salt());
  }

  @Override
  public void onError(Cli cli, Namespace namespace, Throwable e) {
    cli.getStdErr().println(e.getMessage());
  }
}