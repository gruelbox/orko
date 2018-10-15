package com.grahamcrockford.orko.wiring;

import io.dropwizard.setup.Environment;

public interface EnvironmentInitialiser {

  public void init(Environment environment);

}
