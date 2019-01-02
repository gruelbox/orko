package com.gruelbox.orko.app.monolith;

import com.google.inject.Module;
import com.gruelbox.orko.WebHostApplication;

public class MonolithApplication extends WebHostApplication {

  public static void main(final String[] args) throws Exception {
    new MonolithApplication().run(args);
  }

  @Override
  public String getName() {
    return "Orko all-in-one application";
  }

  @Override
  protected Module createApplicationModule() {
    return new MonolithModule();
  }
}