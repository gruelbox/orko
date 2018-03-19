package com.grahamcrockford.oco.api.util;

import io.dropwizard.setup.Environment;

public interface EnvironmentInitialiser {

  public void init(Environment environment);

}
