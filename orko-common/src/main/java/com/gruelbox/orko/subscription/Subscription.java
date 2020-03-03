/**
 * Orko - Copyright © 2018-2019 Graham Crockford
 *
 * <p>This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * <p>You should have received a copy of the GNU Affero General Public License along with this
 * program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.gruelbox.orko.subscription;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gruelbox.orko.spi.TickerSpec;
import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

/**
 * Persistence object for subscriptions.
 *
 * @author Graham Crockford
 */
@Entity(name = Subscription.TABLE_NAME)
final class Subscription {

  static final String TABLE_NAME = "Subscription";
  static final String TICKER_FIELD = "ticker";
  static final String REFERENCE_PRICE_FIELD = "referencePrice";

  @Id
  @Column(name = TICKER_FIELD, nullable = false)
  @NotNull
  @JsonProperty
  private String ticker;

  @Column(name = REFERENCE_PRICE_FIELD)
  @JsonProperty
  private BigDecimal referencePrice;

  Subscription() {
    // Nothing to do
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
