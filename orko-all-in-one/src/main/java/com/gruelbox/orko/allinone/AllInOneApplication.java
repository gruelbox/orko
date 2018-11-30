package com.gruelbox.orko.allinone;

import javax.ws.rs.client.Client;

import com.google.inject.Module;
import com.gruelbox.orko.OrkoConfiguration;
import com.gruelbox.orko.WebHostApplication;

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