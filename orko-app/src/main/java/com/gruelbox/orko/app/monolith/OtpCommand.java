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
    subparser.addArgument(NOCHECK, "-n")
      .nargs("?")
      .setConst(true)
      .help("Skips manual checking, just outputting the new key.");
  }

  @Override
  public void onError(Cli cli, Namespace namespace, Throwable e) {
    cli.getStdErr().println(e.getMessage());
  }
}