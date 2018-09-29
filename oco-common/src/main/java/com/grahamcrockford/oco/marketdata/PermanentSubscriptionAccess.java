package com.grahamcrockford.oco.marketdata;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import com.grahamcrockford.oco.spi.TickerSpec;

public interface PermanentSubscriptionAccess {

  public void add(TickerSpec spec);

  public void remove(TickerSpec spec);

  public Collection<TickerSpec> all();

  public void setReferencePrice(TickerSpec tickerSpec, BigDecimal price);

  public Map<TickerSpec, BigDecimal> getReferencePrices();

}