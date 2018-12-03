package com.gruelbox.orko.jobrun.spi;

import java.util.ServiceLoader;

/**
 * {@link ServiceLoader} bind point for {@link Job} types.
 *
 * @author Graham Crockford
 */
public interface JobTypeContribution {

  /**
   * @return Concrete job classes to be available at runtime.
   */
  Iterable<Class<? extends Job>> jobTypes();

}