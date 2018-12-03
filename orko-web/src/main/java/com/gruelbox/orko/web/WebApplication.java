package com.gruelbox.orko.web;

import com.google.inject.Module;
import com.gruelbox.orko.WebHostApplication;

public class WebApplication extends WebHostApplication {

  public static void main(final String[] args) throws Exception {
    new WebApplication().run(args);
  }

  @Override
  public String getName() {
    return "Orko web API application";
  }

  @Override
  protected Module createApplicationModule() {
    return new WebModule();
  }
}