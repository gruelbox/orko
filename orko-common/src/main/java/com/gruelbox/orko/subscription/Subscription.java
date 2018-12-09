package com.gruelbox.orko.subscription;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gruelbox.orko.spi.TickerSpec;

/**
 * Persistence object for subscriptions.
 *
 * @author Graham Crockford
 */
@Entity(name = Subscription.TABLE_NAME)
final class Subscription {

  static final String TABLE_NAME = "Subscription";
  static final String TICKER = "ticker";
  static final String REFERENCE_PRICE = "referencePrice";

  @Id
  @Column(name = TICKER, nullable = false)
  @NotNull
  @JsonProperty
  private String ticker;

  @Column(name = REFERENCE_PRICE)
  @JsonProperty
  private BigDecimal referencePrice;

  @Version
  @Column(nullable = false)
  private int version;

  Subscription() {

  }

  Subscription(TickerSpec ticker, BigDecimal referencePrice) {
    super();
    this.ticker = ticker.key();
    this.referencePrice = referencePrice;
  }

  TickerSpec getTicker() {
    return TickerSpec.fromKey(ticker);
  }

  BigDecimal getReferencePrice() {
    return referencePrice;
  }

  void setReferencePrice(BigDecimal referencePrice) {
    this.referencePrice = referencePrice;
  }
}