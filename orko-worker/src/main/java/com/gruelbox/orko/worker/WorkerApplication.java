package com.gruelbox.orko.worker;

import javax.ws.rs.client.Client;

import com.google.inject.Module;
import com.gruelbox.orko.BaseApplication;
import com.gruelbox.orko.OrkoConfiguration;

import io.dropwizard.setup.Environment;

public class WorkerApplication extends BaseApplication {

  public static void main(final String[] args) throws Exception {
    new WorkerApplication().run(args);
  }

  @Override
  public String getName() {
    return "Orko background worker";
  }

  @Override
  protected Module createApplicationModule(OrkoConfiguration configuration, Environment environment, Client jerseyClient) {
    return new WorkerModule();
  }
}