package com.gruelbox.orko.exchange;

import static com.gruelbox.orko.exchange.Exchanges.BINANCE;
import static com.gruelbox.orko.exchange.Exchanges.BITFINEX;
import static com.gruelbox.orko.exchange.Exchanges.GDAX;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.knowm.xchange.dto.marketdata.Trade;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.google.common.base.Ticker;
import com.gruelbox.orko.marketdata.ExchangeEventRegistry;
import com.gruelbox.orko.marketdata.MarketDataSubscription;
import com.gruelbox.orko.marketdata.TradeEvent;
import com.gruelbox.orko.notification.NotificationService;
import com.gruelbox.orko.spi.TickerSpec;

import io.reactivex.BackpressureStrategy;
import io.reactivex.subjects.PublishSubject;

public class TestMonitorExchangeSocketHealth {

  private MonitorExchangeSocketHealth monitor;

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
    when(subscription.getTrades())
        .thenReturn(trades.toFlowable(BackpressureStrategy.MISSING));

    when(ticker.read()).thenReturn(MINUTES.toNanos(10));

    monitor = new MonitorExchangeSocketHealth(exchangeEventRegistry,
        notificationService, interval, ticker, t -> failure = t);
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
    verifyZeroInteractions(notificationService);
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
    trades.onNext(TradeEvent.create(TickerSpec.builder().exchange("NOTME").base("BTC").counter("USDT").build(), mock(Trade.class)));

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
    trades.onNext(TradeEvent.create(TickerSpec.builder().exchange(BINANCE).base("BTC").counter("USDT").build(), mock(Trade.class)));
    trades.onNext(TradeEvent.create(TickerSpec.builder().exchange(BITFINEX).base("BTC").counter("USDT").build(), mock(Trade.class)));
    trades.onNext(TradeEvent.create(TickerSpec.builder().exchange(GDAX).base("BTC").counter("USDT").build(), mock(Trade.class)));

    when(ticker.read()).thenReturn(MINUTES.toNanos(28));
    interval.onNext(1);
    verifyZeroInteractions(notificationService);
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
    trades.onNext(TradeEvent.create(TickerSpec.builder().exchange(BINANCE).base("BTC").counter("USDT").build(), mock(Trade.class)));

    when(ticker.read()).thenReturn(MINUTES.toNanos(20));
    interval.onNext(1);
    verify(notificationService).error("Bitfinex trade stream has not sent a trade for 10m");
    verify(notificationService).error("Coinbase Pro trade stream has not sent a trade for 10m");
    verifyNoMoreInteractions(notificationService);
    reset(notificationService);

    when(ticker.read()).thenReturn(MINUTES.toNanos(28));
    interval.onNext(1);
    verifyZeroInteractions(notificationService);
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
