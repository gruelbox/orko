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
package com.gruelbox.orko.exchange;

import static com.gruelbox.orko.exchange.MarketDataType.BALANCE;
import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.DOWN;

import com.google.common.base.MoreObjects;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.gruelbox.orko.exchange.ExchangeEventRegistry.ExchangeEventSubscription;
import com.gruelbox.orko.spi.TickerSpec;
import java.math.BigDecimal;
import org.knowm.xchange.dto.Order.OrderType;
import org.knowm.xchange.dto.meta.CurrencyPairMetaData;

public class MaxTradeAmountCalculator {

  private final ExchangeEventRegistry exchangeEventRegistry;

  private final TickerSpec tickerSpec;
  private final BigDecimal amountStepSize;
  private final Integer priceScale;

  @Inject
  MaxTradeAmountCalculator(
      @Assisted final TickerSpec tickerSpec,
      final ExchangeEventRegistry exchangeEventRegistry,
      final ExchangeService exchangeService) {
    this.tickerSpec = tickerSpec;
    this.exchangeEventRegistry = exchangeEventRegistry;

    CurrencyPairMetaData currencyPairMetaData =
        exchangeService
            .get(tickerSpec.exchange())
            .getExchangeMetaData()
            .getCurrencyPairs()
            .get(tickerSpec.currencyPair());

    this.amountStepSize = currencyPairMetaData.getAmountStepSize();
    this.priceScale = MoreObjects.firstNonNull(currencyPairMetaData.getPriceScale(), 0);
  }

  public BigDecimal adjustAmountForLotSize(BigDecimal amount) {
    if (amountStepSize != null) {
      BigDecimal remainder = amount.remainder(amountStepSize);
      if (remainder.compareTo(ZERO) != 0) {
        return amount.subtract(remainder);
      }
    }
    return amount;
  }

  public BigDecimal validOrderAmount(BigDecimal limitPrice, OrderType direction) {
    BigDecimal result;
    try (ExchangeEventSubscription subscription =
        exchangeEventRegistry.subscribe(MarketDataSubscription.create(tickerSpec, BALANCE))) {
      if (direction.equals(OrderType.ASK)) {
        result = blockingBalance(subscription, tickerSpec.base()).setScale(priceScale, DOWN);
      } else {
        BigDecimal available = blockingBalance(subscription, tickerSpec.counter());
        result = available.divide(limitPrice, priceScale, DOWN);
      }
    }
    return adjustAmountForLotSize(result);
  }

  private BigDecimal blockingBalance(ExchangeEventSubscription subscription, String currency) {
    return subscription
        .getBalances()
        .filter(b -> b.balance().getCurrency().getCurrencyCode().equals(currency))
        .blockingFirst()
        .balance()
        .getAvailable();
  }

  public static class Factory {

    private final ExchangeEventRegistry exchangeEventRegistry;
    private final ExchangeService exchangeService;

    @Inject
    public Factory(ExchangeEventRegistry exchangeEventRegistry, ExchangeService exchangeService) {
      this.exchangeEventRegistry = exchangeEventRegistry;
      this.exchangeService = exchangeService;
    }

    public MaxTradeAmountCalculator create(TickerSpec tickerSpec) {
      return new MaxTradeAmountCalculator(tickerSpec, exchangeEventRegistry, exchangeService);
    }
  }
}
