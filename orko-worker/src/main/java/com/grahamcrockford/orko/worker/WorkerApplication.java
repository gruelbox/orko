package com.grahamcrockford.orko.worker;

import javax.ws.rs.client.Client;

import com.google.inject.Module;
import com.grahamcrockford.orko.BaseApplication;
import com.grahamcrockford.orko.OrkoConfiguration;

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