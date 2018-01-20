package com.grahamcrockford.oco.api;

import org.knowm.xchange.dto.marketdata.Ticker;

public interface AdvancedOrderProcessor<T extends AdvancedOrder> {

  public void tick(T order, Ticker ticker) throws Exception;

}
