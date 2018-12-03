package com.gruelbox.orko.allinone;

import com.google.inject.Module;
import com.gruelbox.orko.WebHostApplication;

public class AllInOneApplication extends WebHostApplication {

  public static void main(final String[] args) throws Exception {
    new AllInOneApplication().run(args);
  }

  @Override
  public String getName() {
    return "Orko all-in-one application";
  }

  @Override
  protected Module createApplicationModule() {
    return new AllInOneModule();
  }
}