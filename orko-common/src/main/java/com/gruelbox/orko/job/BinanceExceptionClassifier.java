package com.gruelbox.orko.job;

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.Callable;

import org.knowm.xchange.binance.dto.BinanceException;

import jersey.repackaged.com.google.common.collect.ImmutableSet;

class BinanceExceptionClassifier {

  private static final int DUPLICATE_ORDER = -2010;

  private static final Set<Integer> RETRIABLE_BINANCE_ERROR_CODES = ImmutableSet.copyOf(Arrays.asList(
      -1000, // UNKNOWN
      -1001, // DISCONNECTED
      -1003, // TOO_MANY_REQUESTS
      -1006, // UNEXPECTED_RESP
      -1007, // TIMEOUT
      -1015, // TOO_MANY_ORDERS
      -1021, // INVALID_TIMESTAMP
      -1022  // INVALID_SIGNATURE
    ));

  static <T> T call(Callable<T> callable) throws DuplicateOrderException, RetriableBinanceException, Exception {
    try {
      return callable.call();
    } catch (BinanceException e) {
      if (e.getCode() == DUPLICATE_ORDER) {
        throw new DuplicateOrderException(e);
      } else if (RETRIABLE_BINANCE_ERROR_CODES.contains(e.getCode())) {
        throw new RetriableBinanceException(e);
      } else {
        throw e;
      }
    }
  }

  static final class RetriableBinanceException extends Exception {

    private static final long serialVersionUID = -5742379073748220801L;

    private RetriableBinanceException(Throwable cause) {
      super(cause);
    }
  }

  static final class DuplicateOrderException extends Exception {

    private static final long serialVersionUID = -1491176560963033335L;

    private DuplicateOrderException(Throwable cause) {
      super(cause);
    }
  }
}
