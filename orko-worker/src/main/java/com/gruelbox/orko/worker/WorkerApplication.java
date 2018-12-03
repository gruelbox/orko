package com.gruelbox.orko.worker;

import com.google.inject.Module;
import com.gruelbox.orko.BaseApplication;

public class WorkerApplication extends BaseApplication {

  public static void main(final String[] args) throws Exception {
    new WorkerApplication().run(args);
  }

  @Override
  public String getName() {
    return "Orko background worker";
  }

  @Override
  protected Module createApplicationModule() {
    return new WorkerModule();
  }
}