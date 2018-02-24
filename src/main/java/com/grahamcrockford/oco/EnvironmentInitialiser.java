package com.grahamcrockford.oco;

import io.dropwizard.setup.Environment;

public interface EnvironmentInitialiser {

  public void init(Environment environment);

}
