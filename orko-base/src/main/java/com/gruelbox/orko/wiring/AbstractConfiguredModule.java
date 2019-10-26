package com.gruelbox.orko.wiring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.gruelbox.tools.dropwizard.guice.Configured;

/**
 * Extend this to either use configuration at bind time, and/or receive configuration
 * and pass it onto others when they are installed.
 *
 * @param <T> Any configuration type, which must be supported by the parent configuration.
 */
public abstract class AbstractConfiguredModule<T> extends AbstractModule implements Configured<T> {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractConfiguredModule.class);

  private T configuration;

  @Override
  public final void setConfiguration(T configuration) {
    LOGGER.info("Configuration set for {}", getClass().getName());
    this.configuration = configuration;
  }

  /**
   * @return The configuration. Can be safely called in {@link #install(Module)}.
   */
  protected T getConfiguration() {
    return configuration;
  }

  @Override
  @SuppressWarnings({"unchecked", "rawtypes"})
  protected void install(Module module) {
    if (module instanceof Configured) {
      LOGGER.info("Delegating configuration from {} to {}", getClass().getName(), module.getClass().getName());
      ((Configured) module).setConfiguration(configuration);
    }
    super.install(module);
  }
}