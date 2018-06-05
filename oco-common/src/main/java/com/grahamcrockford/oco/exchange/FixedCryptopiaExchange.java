package com.grahamcrockford.oco.exchange;

import org.knowm.xchange.cryptopia.CryptopiaExchange;
import org.knowm.xchange.utils.nonce.AtomicLongCurrentTimeIncrementalNonceFactory;

import si.mazi.rescu.SynchronizedValueFactory;

/**
 * TODO contribute this back to xChange
 */
public class FixedCryptopiaExchange extends CryptopiaExchange {

  private final SynchronizedValueFactory<Long> nonceFactory =
      new AtomicLongCurrentTimeIncrementalNonceFactory();

  @Override
  public SynchronizedValueFactory<Long> getNonceFactory() {
    return nonceFactory;
  }
}