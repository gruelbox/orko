package com.gruelbox.orko;

import io.dropwizard.client.JerseyClientConfiguration;

public interface HasJerseyClientConfiguration {

  public JerseyClientConfiguration getJerseyClientConfiguration();

}
