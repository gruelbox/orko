package com.gruelbox.orko.marketdata;

import java.math.BigDecimal;

import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.simulated.SimulatedExchange;

import com.google.common.util.concurrent.Service;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.multibindings.Multibinder;
import com.gruelbox.orko.exchange.ExchangeService;
import com.gruelbox.orko.exchange.Exchanges;

import io.dropwizard.lifecycle.Managed;

public class SimulatorModule extends AbstractModule {

  @Override
  protected void configure() {
    Multibinder.newSetBinder(binder(), Service.class)
        .addBinding().to(SimulatedOrderBookActivity.class);
    Multibinder.newSetBinder(binder(), Managed.class)
        .addBinding().to(DepositCreator.class);
  }

  /**
   * Provides $200,000 to play with
   */
  private static final class DepositCreator implements Managed {

    private final ExchangeService exchangeService;

    @Inject
    DepositCreator(ExchangeService exchangeService) {
      this.exchangeService = exchangeService;
    }

    @Override
    public void start() throws Exception {
      SimulatedExchange exchange = (SimulatedExchange) exchangeService.get(Exchanges.SIMULATED);
      exchange.getAccountService().deposit(Currency.USD, new BigDecimal(200000));
    }

    @Override
    public void stop() throws Exception {
      // No-op
    }
  }
}
