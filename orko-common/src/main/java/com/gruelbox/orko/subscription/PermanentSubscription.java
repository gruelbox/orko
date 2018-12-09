package com.gruelbox.orko.subscription;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Persistence object for subscriptions.
 *
 * @author Graham Crockford
 */
@Entity(name = PermanentSubscription.TABLE_NAME)
final class PermanentSubscription {

  static final String TABLE_NAME = "Subscription";
  static final String TICKER = "ticker";
  static final String REFERENCE_PRICE = "referencePrice";

  @Id
  @Column(name = TICKER, nullable = false)
  @NotNull
  @JsonProperty
  String ticker;

  @Column(name = REFERENCE_PRICE, nullable = true)
  @JsonProperty
  BigDecimal referencePrice;

  PermanentSubscription() {

  }

  PermanentSubscription(String ticker, BigDecimal referencePrice) {
    super();
    this.ticker = ticker;
    this.referencePrice = referencePrice;
  }
}