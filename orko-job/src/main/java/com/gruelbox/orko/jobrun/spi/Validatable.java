package com.gruelbox.orko.jobrun.spi;

/**
 * May be supported by a {@link JobProcessor} if it is capable of updating an
 * unstarted {@link Job} to correspond with current state. Used where
 * a job itself contains a number of other optional jobs which it may
 * start at any time.
 *
 * @author Graham Crockford
 */
public interface Validatable {

  /**
   * Modify a job to fit current state.
   */
  void validate();

}
