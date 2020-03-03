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
package com.gruelbox.orko.monitor;

import static com.gruelbox.orko.exchange.Exchanges.BINANCE;
import static com.gruelbox.orko.exchange.Exchanges.BITFINEX;
import static com.gruelbox.orko.exchange.Exchanges.GDAX;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.google.common.base.Ticker;
import com.gruelbox.orko.exchange.ExchangeEventRegistry;
import com.gruelbox.orko.exchange.MarketDataSubscription;
import com.gruelbox.orko.exchange.TradeEvent;
import com.gruelbox.orko.notification.NotificationService;
import com.gruelbox.orko.spi.TickerSpec;
import io.reactivex.BackpressureStrategy;
import io.reactivex.subjects.PublishSubject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.knowm.xchange.dto.marketdata.Trade;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class TestExchangeSocketHealthMonitor {

  private ExchangeSocketHealthMonitor monitor;

  private final PublishSubject<Integer> interval = PublishSubject.create();
  private final PublishSubject<TradeEvent> trades = PublishSubject.create();
  @Mock private ExchangeEventRegistry exchangeEventRegistry;
  @Mock private ExchangeEventRegistry.ExchangeEventSubscription subscription;
  @Mock private NotificationService notificationService;
  @Mock private Ticker ticker;

  private Throwable failure;

  @Before
  public void setup() throws Exception {
    MockitoAnnotations.initMocks(this);
    when(exchangeEventRegistry.subscribe(Mockito.<MarketDataSubscription[]>any()))
        .thenReturn(subscription);
    when(subscription.getTrades()).thenReturn(trades.toFlowable(BackpressureStrategy.MISSING));

    when(ticker.read()).thenReturn(MINUTES.toNanos(10));

    monitor =
        new ExchangeSocketHealthMonitor(
            exchangeEventRegistry, notificationService, interval, ticker, t -> failure = t);
    monitor.start();
  }

  @After
  public void tearDown() throws Throwable {
    monitor.stop();
    if (failure != null) {
      throw failure;
    }
  }

  @Test
  public void testStartAndStop() {
    // Nothing else to test
  }

  @Test
  public void testTickNothingToDo() {
    when(ticker.read()).thenReturn(MINUTES.toNanos(19));
    interval.onNext(1);
    verifyNoInteractions(notificationService);
  }

  @Test
  public void testTickNotify() {
    when(ticker.read()).thenReturn(MINUTES.toNanos(20));
    interval.onNext(1);
    verify(notificationService).error("Binance trade stream has not sent a trade for 10m");
    verify(notificationService).error("Bitfinex trade stream has not sent a trade for 10m");
    verify(notificationService).error("Coinbase Pro trade stream has not sent a trade for 10m");
  }

  @Test
  public void testTradeInUntrackedExchange() {
    when(ticker.read()).thenReturn(MINUTES.toNanos(19));
    trades.onNext(
        TradeEvent.create(
            TickerSpec.builder().exchange("NOTME").base("BTC").counter("USDT").build(),
            mock(Trade.class)));

    when(ticker.read()).thenReturn(MINUTES.toNanos(20));
    interval.onNext(1);
    verify(notificationService).error("Binance trade stream has not sent a trade for 10m");
    verify(notificationService).error("Bitfinex trade stream has not sent a trade for 10m");
    verify(notificationService).error("Coinbase Pro trade stream has not sent a trade for 10m");
    verifyNoMoreInteractions(notificationService);
  }

  @Test
  public void testTradeResetsAllCounters() {
    when(ticker.read()).thenReturn(MINUTES.toNanos(19));
    trades.onNext(
        TradeEvent.create(
            TickerSpec.builder().exchange(BINANCE).base("BTC").counter("USDT").build(),
            mock(Trade.class)));
    trades.onNext(
        TradeEvent.create(
            TickerSpec.builder().exchange(BITFINEX).base("BTC").counter("USDT").build(),
            mock(Trade.class)));
    trades.onNext(
        TradeEvent.create(
            TickerSpec.builder().exchange(GDAX).base("BTC").counter("USDT").build(),
            mock(Trade.class)));

    when(ticker.read()).thenReturn(MINUTES.toNanos(28));
    interval.onNext(1);
    verifyNoInteractions(notificationService);
    reset(notificationService);

    when(ticker.read()).thenReturn(MINUTES.toNanos(29));
    interval.onNext(1);
    verify(notificationService).error("Binance trade stream has not sent a trade for 10m");
    verify(notificationService).error("Bitfinex trade stream has not sent a trade for 10m");
    verify(notificationService).error("Coinbase Pro trade stream has not sent a trade for 10m");
    verifyNoMoreInteractions(notificationService);
    reset(notificationService);
  }

  @Test
  public void testTradeResetsIndividualCounter() {
    when(ticker.read()).thenReturn(MINUTES.toNanos(19));
    trades.onNext(
        TradeEvent.create(
            TickerSpec.builder().exchange(BINANCE).base("BTC").counter("USDT").build(),
            mock(Trade.class)));

    when(ticker.read()).thenReturn(MINUTES.toNanos(20));
    interval.onNext(1);
    verify(notificationService).error("Bitfinex trade stream has not sent a trade for 10m");
    verify(notificationService).error("Coinbase Pro trade stream has not sent a trade for 10m");
    verifyNoMoreInteractions(notificationService);
    reset(notificationService);

    when(ticker.read()).thenReturn(MINUTES.toNanos(28));
    interval.onNext(1);
    verifyNoInteractions(notificationService);
    reset(notificationService);

    when(ticker.read()).thenReturn(MINUTES.toNanos(29));
    interval.onNext(1);
    verify(notificationService).error("Binance trade stream has not sent a trade for 10m");
    verifyNoMoreInteractions(notificationService);
    reset(notificationService);

    when(ticker.read()).thenReturn(MINUTES.toNanos(30));
    interval.onNext(1);
    verify(notificationService).error("Bitfinex trade stream has not sent a trade for 10m");
    verify(notificationService).error("Coinbase Pro trade stream has not sent a trade for 10m");
    verifyNoMoreInteractions(notificationService);
  }
}
