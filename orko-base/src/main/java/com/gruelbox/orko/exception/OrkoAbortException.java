package com.gruelbox.orko.exception;

/**
 * Signals a controlled shutdown.
 *
 * @author Graham Crockford
 */
public class OrkoAbortException extends Exception {

  private static final long serialVersionUID = 7682887438473803862L;

  public OrkoAbortException(String message) {
    super(message);
  }
}