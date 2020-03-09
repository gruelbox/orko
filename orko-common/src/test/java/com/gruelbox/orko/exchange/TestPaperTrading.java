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

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.knowm.xchange.currency.Currency.BTC;
import static org.knowm.xchange.currency.Currency.ETH;
import static org.knowm.xchange.currency.Currency.USD;
import static org.knowm.xchange.currency.CurrencyPair.BTC_USD;
import static org.knowm.xchange.currency.CurrencyPair.ETH_USD;
import static org.knowm.xchange.dto.Order.OrderStatus.NEW;
import static org.knowm.xchange.dto.Order.OrderType.ASK;
import static org.knowm.xchange.dto.Order.OrderType.BID;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import com.gruelbox.orko.spi.TickerSpec;
import io.reactivex.BackpressureStrategy;
import io.reactivex.subjects.PublishSubject;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.account.Balance;
import org.knowm.xchange.dto.account.Wallet;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.marketdata.Trade;
import org.knowm.xchange.dto.meta.CurrencyMetaData;
import org.knowm.xchange.dto.meta.ExchangeMetaData;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.exceptions.ExchangeException;
import org.knowm.xchange.service.trade.params.CancelOrderParams;
import org.knowm.xchange.service.trade.params.DefaultCancelOrderParamId;
import org.knowm.xchange.service.trade.params.TradeHistoryParamCurrencyPair;
import org.knowm.xchange.service.trade.params.TradeHistoryParams;
import org.knowm.xchange.service.trade.params.orders.OpenOrdersParamCurrencyPair;
import org.knowm.xchange.service.trade.params.orders.OpenOrdersParams;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class TestPaperTrading {

  private static final BigDecimal EXPECTED_INITIAL_BALANCE = new BigDecimal(1000);
  private static final String ORDER_ID = "123";
  private static final String EXCHANGE = "foobit";

  @Mock private ExchangeEventRegistry exchangeEventRegistry;
  @Mock private ExchangeService exchangeService;
  @Mock private Exchange exchange;
  @Mock private ExchangeMetaData exchangeMetaData;
  @Mock private CurrencyMetaData currencyMetaData;
  @Mock private ExchangeEventRegistry.ExchangeEventSubscription subscription;

  private final PublishSubject<TickerEvent> tickerSubject = PublishSubject.create();

  private PaperTradeService tradeService;
  private PaperAccountService accountService;

  @SuppressWarnings("unchecked")
  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
    when(exchangeService.get(EXCHANGE)).thenReturn(exchange);
    when(exchange.getExchangeMetaData()).thenReturn(exchangeMetaData);
    when(exchangeMetaData.getCurrencies())
        .thenReturn(
            ImmutableMap.of(
                BTC, currencyMetaData,
                USD, currencyMetaData));
    when(exchangeEventRegistry.subscribe()).thenReturn(subscription);
    when(subscription.replace(Mockito.any(Set.class))).thenReturn(subscription);
    when(subscription.getTickers())
        .thenReturn(tickerSubject.toFlowable(BackpressureStrategy.BUFFER));

    PaperAccountService.Factory accountServiceFactory =
        new PaperAccountService.Factory(exchangeService);
    accountService = accountServiceFactory.getForExchange(EXCHANGE);
    tradeService =
        (PaperTradeService)
            new PaperTradeService.Factory(exchangeEventRegistry, accountServiceFactory)
                .getForExchange(EXCHANGE);
  }

  @After
  public void tearDown() {
    tradeService.shutdown();
  }

  @Test(expected = ExchangeException.class)
  public void testInitialOpenOrdersInvalidParameter() throws IOException {
    tradeService.getOpenOrders(
        new OpenOrdersParams() {
          @Override
          public boolean accept(LimitOrder order) {
            return false;
          }
        });
  }

  @Test(expected = ExchangeException.class)
  public void testInitialTradeHistoryInvalidParameter() throws IOException {
    tradeService.getTradeHistory(new TradeHistoryParams() {});
  }

  @Test(expected = ExchangeException.class)
  public void testInitialCancelOrderById() throws IOException {
    tradeService.cancelOrder(ORDER_ID);
  }

  @Test(expected = ExchangeException.class)
  public void testInitialCancelOrderByInvalidParameter() throws IOException {
    tradeService.cancelOrder(new CancelOrderParams() {});
  }

  @Test(expected = ExchangeException.class)
  public void testInitialCancelOrderByParamId() throws IOException {
    tradeService.cancelOrder(new DefaultCancelOrderParamId(ORDER_ID));
  }

  /** Checks that the initial account state is correct with no trades having been placed. */
  @Test
  public void testInitialState() throws IOException {
    assertThat(tradeService.getOpenOrders().getAllOpenOrders(), empty());
    assertThat(tradeService.getOpenOrders(openOrdersParams()).getAllOpenOrders(), empty());
    assertThat(tradeService.getTradeHistory(tradeHistoryParams()).getTrades(), empty());
    Wallet wallet = accountService.getAccountInfo().getWallet();
    Balance btcBalance = wallet.getBalance(BTC);
    Balance usdBalance = wallet.getBalance(USD);
    assertThat(btcBalance.getAvailable(), equalTo(EXPECTED_INITIAL_BALANCE));
    assertThat(usdBalance.getAvailable(), equalTo(EXPECTED_INITIAL_BALANCE));
    assertThat(btcBalance.getTotal(), equalTo(EXPECTED_INITIAL_BALANCE));
    assertThat(usdBalance.getTotal(), equalTo(EXPECTED_INITIAL_BALANCE));
  }

  /**
   * Tests the order/trade workflow to ensure that balances, open orders and trades are correctly
   * reported at each stage and matching is processed correctly
   */
  @Test
  public void testPlaceLimitOrderAsk() throws Exception {
    // Place a sell order
    BigDecimal limitPrice = new BigDecimal(100);
    BigDecimal amount = new BigDecimal(5);
    BigDecimal counterAmount = amount.multiply(limitPrice);
    String id =
        tradeService.placeLimitOrder(
            new LimitOrder.Builder(ASK, BTC_USD)
                .limitPrice(limitPrice)
                .originalAmount(amount)
                .build());

    // Orders which should not fill
    tradeService.placeLimitOrder(
        new LimitOrder.Builder(ASK, ETH_USD).limitPrice(limitPrice).originalAmount(amount).build());
    tradeService.placeLimitOrder(
        new LimitOrder.Builder(ASK, BTC_USD)
            .limitPrice(limitPrice.add(new BigDecimal("0.02")))
            .originalAmount(ONE)
            .build());
    tradeService.placeLimitOrder(
        new LimitOrder.Builder(BID, BTC_USD)
            .limitPrice(new BigDecimal("0.03"))
            .originalAmount(ONE)
            .build());

    // Check all state matches
    ThrowingRunnable checkUnchangedState =
        () -> {
          List<Order> allOpenOrders = tradeService.getOpenOrders().getAllOpenOrders();
          assertThat(allOpenOrders, hasSize(4));
          LimitOrder order =
              (LimitOrder)
                  allOpenOrders.stream()
                      .filter(o -> o.getId().equals(id))
                      .reduce(
                          (a, b) -> {
                            fail("Multiple orders with same id");
                            return null;
                          })
                      .get();
          assertThat(order.getStatus(), equalTo(NEW));
          assertThat(order.getType(), equalTo(ASK));
          assertThat(order.getLimitPrice(), equalTo(limitPrice));
          assertThat(order.getOriginalAmount(), equalTo(amount));
          assertThat(order.getCumulativeAmount(), equalTo(new BigDecimal(0)));

          assertThat(tradeService.getOpenOrders(openOrdersParams()).getAllOpenOrders(), hasSize(3));
          assertThat(tradeService.getTradeHistory(tradeHistoryParams()).getTrades(), empty());
          Wallet wallet = accountService.getAccountInfo().getWallet();
          Balance btcBalance = wallet.getBalance(BTC);
          Balance ethBalance = wallet.getBalance(ETH);
          Balance usdBalance = wallet.getBalance(USD);

          assertThat(
              btcBalance.getAvailable(),
              equalTo(EXPECTED_INITIAL_BALANCE.subtract(amount).subtract(ONE)));
          assertThat(btcBalance.getTotal(), equalTo(EXPECTED_INITIAL_BALANCE));
          assertThat(btcBalance.getFrozen(), equalTo(amount.add(ONE)));

          assertThat(ethBalance.getAvailable(), equalTo(EXPECTED_INITIAL_BALANCE.subtract(amount)));
          assertThat(ethBalance.getTotal(), equalTo(EXPECTED_INITIAL_BALANCE));
          assertThat(ethBalance.getFrozen(), equalTo(amount));

          assertThat(
              usdBalance.getAvailable(),
              equalTo(EXPECTED_INITIAL_BALANCE.subtract(new BigDecimal("0.03"))));
          assertThat(usdBalance.getTotal(), equalTo(EXPECTED_INITIAL_BALANCE));
          assertThat(usdBalance.getFrozen(), equalTo(new BigDecimal("0.03")));
        };

    checkUnchangedState.run();

    // Fire a ticker below our target price
    BigDecimal insufficientPrice = new BigDecimal("99.99");
    fireTicker(
        new Ticker.Builder()
            .ask(insufficientPrice)
            .bid(insufficientPrice)
            .last(insufficientPrice)
            .build());

    // Check nothing changed
    checkUnchangedState.run();

    // Now fill the order
    fireTicker(
        new Ticker.Builder()
            .ask(insufficientPrice)
            .bid(limitPrice)
            .last(insufficientPrice)
            .build());

    // Check end state
    assertThat(tradeService.getOpenOrders().getAllOpenOrders(), hasSize(3));
    assertThat(tradeService.getOpenOrders(openOrdersParams()).getAllOpenOrders(), hasSize(2));
    List<Trade> trades = tradeService.getTradeHistory(tradeHistoryParams()).getTrades();
    assertThat(trades, hasSize(1));
    Trade trade = trades.get(0);
    assertThat(trade.getCurrencyPair(), equalTo(BTC_USD));
    assertThat(trade.getId(), notNullValue());
    assertThat(trade.getId(), not(""));
    assertThat(trade.getOriginalAmount(), equalTo(amount));
    assertThat(trade.getPrice(), equalTo(limitPrice));
    assertThat(trade.getType(), equalTo(ASK));
    assertThat(trade.getTimestamp(), notNullValue());
    Wallet wallet = accountService.getAccountInfo().getWallet();
    Balance btcBalance = wallet.getBalance(BTC);
    Balance ethBalance = wallet.getBalance(ETH);
    Balance usdBalance = wallet.getBalance(USD);
    assertThat(
        btcBalance.getAvailable(),
        equalTo(EXPECTED_INITIAL_BALANCE.subtract(amount).subtract(ONE)));
    assertThat(btcBalance.getTotal(), equalTo(EXPECTED_INITIAL_BALANCE.subtract(amount)));
    assertThat(btcBalance.getFrozen(), equalTo(ONE));
    assertThat(ethBalance.getAvailable(), equalTo(EXPECTED_INITIAL_BALANCE.subtract(amount)));
    assertThat(ethBalance.getTotal(), equalTo(EXPECTED_INITIAL_BALANCE));
    assertThat(ethBalance.getFrozen(), equalTo(amount));
    assertThat(
        usdBalance.getAvailable(),
        equalTo(EXPECTED_INITIAL_BALANCE.add(counterAmount).subtract(new BigDecimal("0.03"))));
    assertThat(usdBalance.getTotal(), equalTo(EXPECTED_INITIAL_BALANCE.add(counterAmount)));
    assertThat(usdBalance.getFrozen(), equalTo(new BigDecimal("0.03")));
  }

  /** Confirms that a specific order is processed correctly when buying instead of selling. */
  @Test
  public void testPlaceLimitOrderBid() throws Exception {

    // Place a buy order
    BigDecimal limitPrice = new BigDecimal(100);
    BigDecimal amount = new BigDecimal(5);
    BigDecimal counterAmount = amount.multiply(limitPrice);
    String id =
        tradeService.placeLimitOrder(
            new LimitOrder.Builder(BID, BTC_USD)
                .limitPrice(limitPrice)
                .originalAmount(amount)
                .build());

    // Check all state matches
    ThrowingRunnable checkUnchangedState =
        () -> {
          List<Order> allOpenOrders = tradeService.getOpenOrders().getAllOpenOrders();
          assertThat(allOpenOrders, hasSize(1));
          LimitOrder order = (LimitOrder) allOpenOrders.get(0);
          assertThat(order.getId(), equalTo(id));
          assertThat(order.getStatus(), equalTo(NEW));
          assertThat(order.getType(), equalTo(BID));
          assertThat(order.getLimitPrice(), equalTo(limitPrice));
          assertThat(order.getOriginalAmount(), equalTo(amount));
          assertThat(order.getCumulativeAmount(), equalTo(new BigDecimal(0)));

          assertThat(
              tradeService.getOpenOrders(openOrdersParams()).getAllOpenOrders(), contains(order));
          assertThat(tradeService.getTradeHistory(tradeHistoryParams()).getTrades(), empty());
          Wallet wallet = accountService.getAccountInfo().getWallet();
          Balance btcBalance = wallet.getBalance(BTC);
          Balance usdBalance = wallet.getBalance(USD);

          assertThat(btcBalance.getAvailable(), equalTo(EXPECTED_INITIAL_BALANCE));
          assertThat(btcBalance.getTotal(), equalTo(EXPECTED_INITIAL_BALANCE));
          assertThat(btcBalance.getFrozen(), equalTo(ZERO));

          assertThat(
              usdBalance.getAvailable(), equalTo(EXPECTED_INITIAL_BALANCE.subtract(counterAmount)));
          assertThat(usdBalance.getTotal(), equalTo(EXPECTED_INITIAL_BALANCE));
          assertThat(usdBalance.getFrozen(), equalTo(counterAmount));
        };

    checkUnchangedState.run();

    // Fire a ticker above our target price
    BigDecimal insufficientPrice = new BigDecimal("100.01");
    fireTicker(
        new Ticker.Builder()
            .ask(insufficientPrice)
            .bid(insufficientPrice)
            .last(insufficientPrice)
            .build());

    // Check nothing changed
    checkUnchangedState.run();

    // Fire a ticker at our target price
    fireTicker(
        new Ticker.Builder()
            .ask(limitPrice)
            .bid(insufficientPrice)
            .last(insufficientPrice)
            .build());

    // Check end state
    assertThat(tradeService.getOpenOrders().getAllOpenOrders(), empty());
    assertThat(tradeService.getOpenOrders(openOrdersParams()).getAllOpenOrders(), empty());
    List<Trade> trades = tradeService.getTradeHistory(tradeHistoryParams()).getTrades();
    assertThat(trades, hasSize(1));
    Trade trade = trades.get(0);
    assertThat(trade.getCurrencyPair(), equalTo(BTC_USD));
    assertThat(trade.getId(), notNullValue());
    assertThat(trade.getId(), not(""));
    assertThat(trade.getOriginalAmount(), equalTo(amount));
    assertThat(trade.getPrice(), equalTo(limitPrice));
    assertThat(trade.getType(), equalTo(BID));
    assertThat(trade.getTimestamp(), notNullValue());
    Wallet wallet = accountService.getAccountInfo().getWallet();
    Balance btcBalance = wallet.getBalance(BTC);
    Balance usdBalance = wallet.getBalance(USD);
    assertThat(btcBalance.getAvailable(), equalTo(EXPECTED_INITIAL_BALANCE.add(amount)));
    assertThat(btcBalance.getTotal(), equalTo(EXPECTED_INITIAL_BALANCE.add(amount)));
    assertThat(btcBalance.getFrozen(), equalTo(ZERO));
    assertThat(
        usdBalance.getAvailable(), equalTo(EXPECTED_INITIAL_BALANCE.subtract(counterAmount)));
    assertThat(usdBalance.getTotal(), equalTo(EXPECTED_INITIAL_BALANCE.subtract(counterAmount)));
    assertThat(usdBalance.getFrozen(), equalTo(ZERO));
  }

  @Test
  public void testCancelBuyOrder() throws IOException {

    // Place a buy order
    String id =
        tradeService.placeLimitOrder(
            new LimitOrder.Builder(BID, BTC_USD)
                .limitPrice(new BigDecimal(100))
                .originalAmount(new BigDecimal(5))
                .build());

    // Cancel it
    tradeService.cancelOrder(id);

    // Make sure we're back where we started.
    testInitialState();
  }

  @Test
  public void testCancelSellOrder() throws IOException {

    // Place a sell order
    String id =
        tradeService.placeLimitOrder(
            new LimitOrder.Builder(ASK, BTC_USD)
                .limitPrice(new BigDecimal(100))
                .originalAmount(new BigDecimal(5))
                .build());

    // Cancel it
    tradeService.cancelOrder(id);

    // Make sure we're back where we started.
    testInitialState();
  }

  private OpenOrdersParamCurrencyPair openOrdersParams() {
    OpenOrdersParamCurrencyPair params = tradeService.createOpenOrdersParams();
    params.setCurrencyPair(BTC_USD);
    return params;
  }

  private TradeHistoryParamCurrencyPair tradeHistoryParams() {
    TradeHistoryParamCurrencyPair params = tradeService.createTradeHistoryParams();
    params.setCurrencyPair(BTC_USD);
    return params;
  }

  private void fireTicker(Ticker ticker) {
    tickerSubject.onNext(
        TickerEvent.create(
            TickerSpec.builder().base("BTC").counter("USD").exchange(EXCHANGE).build(), ticker));
  }

  private interface ThrowingRunnable {
    void run() throws Exception;
  }
}
