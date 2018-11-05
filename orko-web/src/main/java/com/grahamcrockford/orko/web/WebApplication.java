package com.grahamcrockford.orko.web;

import javax.ws.rs.client.Client;

import com.google.inject.Module;
import com.grahamcrockford.orko.OrkoConfiguration;
import com.grahamcrockford.orko.WebHostApplication;

import io.dropwizard.setup.Environment;

public class WebApplication extends WebHostApplication {

  public static void main(final String[] args) throws Exception {
    new WebApplication().run(args);
  }

  @Override
  public String getName() {
    return "Orko web API application";
  }

  @Override
  protected Module createApplicationModule(OrkoConfiguration configuration, Environment environment, Client jerseyClient) {
    return new WebModule();
  }
}