package com.grahamcrockford.oco.core;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.IOError;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.service.marketdata.MarketDataService;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import com.grahamcrockford.oco.OcoConfiguration;
import com.grahamcrockford.oco.api.AdvancedOrder;
import com.grahamcrockford.oco.api.AdvancedOrderInfo;
import com.grahamcrockford.oco.api.AdvancedOrderProcessor;
import com.grahamcrockford.oco.core.ExchangeService;
import com.grahamcrockford.oco.core.GameLoop;
import com.grahamcrockford.oco.db.AdvancedOrderPersistenceService;

public class TestGameLoop {

  private static final String BASE1 = "FOO";
  private static final String BASE2 = "BAR";
  private static final String COUNTER = "USDT";
  private static final String EXCHANGE = "fooex";

  private final MockOrder job1 = new MockOrder(1L, BASE1);
  private final MockOrder job2 = new MockOrder(2L, BASE2);

  @Mock private OcoConfiguration ocoConfiguration;
  @Mock private AdvancedOrderPersistenceService persistenceService;
  @Mock private ExchangeService exchangeService;

  @Mock private Exchange exchange;
  @Mock private MarketDataService marketDataService;
  @Mock private TelegramService telegramService;
  @Mock private Injector injector;
  @Mock private MockOrder.Processor processor;

  private GameLoop gameLoop;


  @Before
  public void before() {
    MockitoAnnotations.initMocks(this);
    gameLoop = new GameLoop(ocoConfiguration, persistenceService, exchangeService, injector, telegramService);
    when(injector.getInstance(MockOrder.Processor.class)).thenReturn(processor);
  }

  /**
   * Base case. Nothing to do.
   */
  @Test
  public void testNoJobs() throws Exception {
    when(persistenceService.listJobs()).thenReturn(Collections.emptyList());

    gameLoop.runOneIteration();

    verifyDidNothing();
  }

  /**
   * Make sure we can't kill the game loop with a database error.
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testOuterException() throws Exception {
    when(persistenceService.listJobs()).thenThrow(IOError.class);

    gameLoop.runOneIteration();

    verify(telegramService).sendMessage(Mockito.anyString());
    verifyZeroInteractions(exchangeService, processor);
  }

  /**
   * Make sure we continue to process the next job if the first one fails.
   *
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testInnerException() throws Exception {
    when(persistenceService.listJobs()).thenReturn(ImmutableList.of(job1, job2));
    mockExchange(EXCHANGE);

    final Ticker ticker2 = new Ticker.Builder().ask(BigDecimal.ZERO).build();
    when(marketDataService.getTicker(new CurrencyPair(BASE1, COUNTER))).thenThrow(IOError.class);
    when(marketDataService.getTicker(new CurrencyPair(BASE2, COUNTER))).thenReturn(ticker2);

    gameLoop.runOneIteration();

    verifyTriedAndFailedJob1AndSucceededJob2(ticker2);
  }

  /**
   * Tests the backoff is exponential for poison jobs.
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testBackoff() throws Exception {
    when(persistenceService.listJobs()).thenReturn(ImmutableList.of(job1, job2));
    mockExchange(EXCHANGE);

    final Ticker ticker2 = new Ticker.Builder().ask(BigDecimal.ZERO).build();
    when(marketDataService.getTicker(new CurrencyPair(BASE1, COUNTER))).thenThrow(IOError.class);
    when(marketDataService.getTicker(new CurrencyPair(BASE2, COUNTER))).thenReturn(ticker2);

    // Attempt 1 - TRY
    gameLoop.runOneIteration();
    verifyTriedAndFailedJob1AndSucceededJob2(ticker2);

    resetAll();
    when(marketDataService.getTicker(new CurrencyPair(BASE1, COUNTER))).thenThrow(IOError.class);
    when(marketDataService.getTicker(new CurrencyPair(BASE2, COUNTER))).thenReturn(ticker2);

    // Attempt 2 - skip
    gameLoop.runOneIteration();
    verifyTriedJob2(ticker2);

    resetAll();
    when(marketDataService.getTicker(new CurrencyPair(BASE1, COUNTER))).thenThrow(IOError.class);
    when(marketDataService.getTicker(new CurrencyPair(BASE2, COUNTER))).thenReturn(ticker2);

    // Attempt 3 - TRY
    gameLoop.runOneIteration();
    verifyTriedAndFailedJob1AndSucceededJob2(ticker2);

    resetAll();
    when(marketDataService.getTicker(new CurrencyPair(BASE1, COUNTER))).thenThrow(IOError.class);
    when(marketDataService.getTicker(new CurrencyPair(BASE2, COUNTER))).thenReturn(ticker2);

    // Attempt 4 - skip
    gameLoop.runOneIteration();
    verifyTriedJob2(ticker2);

    resetAll();
    when(marketDataService.getTicker(new CurrencyPair(BASE1, COUNTER))).thenThrow(IOError.class);
    when(marketDataService.getTicker(new CurrencyPair(BASE2, COUNTER))).thenReturn(ticker2);

    when(persistenceService.listJobs()).thenReturn(ImmutableList.of(job1, job2));

    // Attempt 5 - skip
    gameLoop.runOneIteration();
    verifyTriedJob2(ticker2);

    resetAll();
    when(marketDataService.getTicker(new CurrencyPair(BASE1, COUNTER))).thenThrow(IOError.class);
    when(marketDataService.getTicker(new CurrencyPair(BASE2, COUNTER))).thenReturn(ticker2);

    // Attempt 6 - TRY
    gameLoop.runOneIteration();
    verifyTriedAndFailedJob1AndSucceededJob2(ticker2);

    resetAll();
    when(marketDataService.getTicker(new CurrencyPair(BASE1, COUNTER))).thenThrow(IOError.class);
    when(marketDataService.getTicker(new CurrencyPair(BASE2, COUNTER))).thenReturn(ticker2);

    // Attempt 7 - skip
    gameLoop.runOneIteration();
    verifyTriedJob2(ticker2);

    resetAll();
    when(marketDataService.getTicker(new CurrencyPair(BASE1, COUNTER))).thenThrow(IOError.class);
    when(marketDataService.getTicker(new CurrencyPair(BASE2, COUNTER))).thenReturn(ticker2);

    // Attempt 8 - skip
    gameLoop.runOneIteration();
    verifyTriedJob2(ticker2);

    resetAll();
    when(marketDataService.getTicker(new CurrencyPair(BASE1, COUNTER))).thenThrow(IOError.class);
    when(marketDataService.getTicker(new CurrencyPair(BASE2, COUNTER))).thenReturn(ticker2);

    // Attempt 9 - skip
    gameLoop.runOneIteration();
    verifyTriedJob2(ticker2);

    resetAll();
    when(marketDataService.getTicker(new CurrencyPair(BASE1, COUNTER))).thenThrow(IOError.class);
    when(marketDataService.getTicker(new CurrencyPair(BASE2, COUNTER))).thenReturn(ticker2);

    // Attempt 10 - skip
    gameLoop.runOneIteration();
    verifyTriedJob2(ticker2);

    resetAll();
    when(marketDataService.getTicker(new CurrencyPair(BASE1, COUNTER))).thenThrow(IOError.class);
    when(marketDataService.getTicker(new CurrencyPair(BASE2, COUNTER))).thenReturn(ticker2);

    // Attempt 11 - try
    gameLoop.runOneIteration();
    verifyTriedAndFailedJob1AndSucceededJob2(ticker2);
  }

  /**
   * Once we hit a skip count of 16, we should cap out there.
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testLimitToExponentialBackOff() throws Exception {
    when(persistenceService.listJobs()).thenReturn(ImmutableList.of(job1, job2));
    mockExchange(EXCHANGE);

    final Ticker ticker2 = new Ticker.Builder().ask(BigDecimal.ZERO).build();
    when(marketDataService.getTicker(new CurrencyPair(BASE1, COUNTER))).thenThrow(IOError.class);
    when(marketDataService.getTicker(new CurrencyPair(BASE2, COUNTER))).thenReturn(ticker2);

    // Should run on attempts
    /*   1
          1
         3
          2
         6
          4
         11
          8
         20
          16 ----- max backoff
         37
          16
         54
          16
         71  ----- total 8 */
    for (int i = 0 ; i < 71 ; i++) {
      gameLoop.runOneIteration();
    }

    verify(marketDataService, times(8)).getTicker(new CurrencyPair(BASE1, COUNTER));
    verify(marketDataService, times(71)).getTicker(new CurrencyPair(BASE2, COUNTER));
    verify(telegramService, times(8)).sendMessage(Mockito.anyString());
    verify(processor, times(71)).tick(job2, ticker2);
    verifyNoMoreInteractions(marketDataService, telegramService, processor);
  }

  /**
   * Normal invocation of two jobs.
   */
  @Test
  public void testInvokeProcessors() throws Exception {
    when(persistenceService.listJobs()).thenReturn(ImmutableList.of(job1, job2));
    mockExchange(EXCHANGE);

    final Ticker ticker1 = new Ticker.Builder().ask(BigDecimal.ZERO).build();
    final Ticker ticker2 = new Ticker.Builder().ask(BigDecimal.ONE).build();
    when(marketDataService.getTicker(new CurrencyPair(BASE1, COUNTER))).thenReturn(ticker1);
    when(marketDataService.getTicker(new CurrencyPair(BASE2, COUNTER))).thenReturn(ticker2);

    gameLoop.runOneIteration();

    verify(marketDataService).getTicker(new CurrencyPair(BASE1, COUNTER));
    verify(marketDataService).getTicker(new CurrencyPair(BASE2, COUNTER));
    verify(processor).tick(job1, ticker1);
    verify(processor).tick(job2, ticker2);
    verifyNoMoreInteractions(processor);
  }

  /**
   * Make sure we aren't violating any validation.
   */
  @Test
  public void testScheduler() {
    when(ocoConfiguration.getLoopSeconds()).thenReturn(1);
    gameLoop.scheduler();
  }

  /* --------------------------------------------------------------------------------------------------------------- */

  private void resetAll() throws IOException {
    reset(marketDataService, telegramService, processor);
  }

  private void verifyTriedJob2(final Ticker ticker2) throws IOException, Exception {
    verify(marketDataService).getTicker(new CurrencyPair(BASE2, COUNTER));
    verify(processor).tick(job2, ticker2);
    verifyNoMoreInteractions(marketDataService, telegramService, processor);
  }

  private void verifyTriedAndFailedJob1AndSucceededJob2(final Ticker ticker2) throws IOException, Exception {
    verify(marketDataService).getTicker(new CurrencyPair(BASE1, COUNTER));
    verify(marketDataService).getTicker(new CurrencyPair(BASE2, COUNTER));
    verify(telegramService).sendMessage(Mockito.anyString());
    verify(processor).tick(job2, ticker2);
    verifyNoMoreInteractions(marketDataService, telegramService, processor);
  }

  private void verifyDidNothing() {
    verify(persistenceService).listJobs();
    verifyZeroInteractions(exchangeService, processor);
  }

  private void mockExchange(String exchangeCode) throws IOException {
    when(exchangeService.get(exchangeCode)).thenReturn(exchange);
    when(exchange.getMarketDataService()).thenReturn(marketDataService);
  }

  /* --------------------------------------------------------------------------------------------------------------- */

  /**
   * Simple order type for testing.
   */
  private static final class MockOrder implements AdvancedOrder {

    private final AdvancedOrderInfo exchangeInfo;
    private final long id;

    MockOrder(long id, String base) {
      this.id = id;
      exchangeInfo = AdvancedOrderInfo.builder().base(base).counter(COUNTER).exchange(EXCHANGE).build();
    }

    @Override
    public long id() {
      return id;
    }

    @Override
    public AdvancedOrderInfo basic() {
      return exchangeInfo;
    }

    @Override
    public Class<Processor> processor() {
      return Processor.class;
    }

    private interface Processor extends AdvancedOrderProcessor<MockOrder> { }

  }
}