package com.grahamcrockford.oco.api;

import org.knowm.xchange.dto.marketdata.Ticker;

import com.grahamcrockford.oco.db.QueueAccess;

public interface AdvancedOrderProcessor<T extends AdvancedOrder> {

  public void tick(T order, Ticker ticker, QueueAccess<AdvancedOrder> queueAccess) throws Exception;

}
