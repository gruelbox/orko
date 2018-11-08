package com.grahamcrockford.orko.allinone;

import javax.ws.rs.client.Client;

import com.google.inject.Module;
import com.grahamcrockford.orko.OrkoConfiguration;
import com.grahamcrockford.orko.WebHostApplication;
import io.dropwizard.setup.Environment;

public class AllInOneApplication extends WebHostApplication {

  public static void main(final String[] args) throws Exception {
    new AllInOneApplication().run(args);
  }

  @Override
  public String getName() {
    return "Orko all-in-one application";
  }

  @Override
  protected Module createApplicationModule(OrkoConfiguration configuration, Environment environment, Client jerseyClient) {
    return new AllInOneModule(configuration);
  }
}