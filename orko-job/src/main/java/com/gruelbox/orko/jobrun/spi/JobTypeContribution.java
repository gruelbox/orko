package com.gruelbox.orko.jobrun.spi;

import java.util.ServiceLoader;

/**
 * {@link ServiceLoader} bind point for {@link Job} types. This needs
 * to be static in order to work with {@link JobTypeResolver}, which
 * is instantiated outside the injector by Jackson.
 *
 * @author Graham Crockford
 */
public interface JobTypeContribution {

  /**
   * @return Concrete job classes to be available at runtime.
   */
  Iterable<Class<? extends Job>> jobTypes();

}