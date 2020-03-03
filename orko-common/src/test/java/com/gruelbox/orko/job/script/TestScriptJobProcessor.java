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
package com.gruelbox.orko.job.script;

import static com.gruelbox.orko.exchange.MarketDataType.TICKER;
import static com.gruelbox.orko.jobrun.spi.Status.FAILURE_PERMANENT;
import static com.gruelbox.orko.jobrun.spi.Status.RUNNING;
import static com.gruelbox.orko.jobrun.spi.Status.SUCCESS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import com.gruelbox.orko.auth.Hasher;
import com.gruelbox.orko.db.Transactionally;
import com.gruelbox.orko.exchange.ExchangeEventRegistry;
import com.gruelbox.orko.exchange.ExchangeEventRegistry.ExchangeEventSubscription;
import com.gruelbox.orko.exchange.MarketDataSubscription;
import com.gruelbox.orko.exchange.TickerEvent;
import com.gruelbox.orko.job.LimitOrderJob;
import com.gruelbox.orko.job.LimitOrderJob.Direction;
import com.gruelbox.orko.job.script.ScriptJob.Builder;
import com.gruelbox.orko.jobrun.JobSubmitter;
import com.gruelbox.orko.jobrun.spi.JobControl;
import com.gruelbox.orko.jobrun.spi.Status;
import com.gruelbox.orko.notification.NotificationService;
import com.gruelbox.orko.spi.TickerSpec;
import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.script.ScriptException;
import org.junit.Before;
import org.junit.Test;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class TestScriptJobProcessor {

  private static final String SCRIPT_SIGNING_KEY = "WHATEVER REALLY. DOESN'T MATTER";
  @Mock private JobControl jobControl;
  @Mock private ExchangeEventRegistry exchangeEventRegistry;
  @Mock private NotificationService notificationService;
  @Mock private JobSubmitter jobSubmitter;
  @Mock private Transactionally transactionally;
  private final Hasher hasher = new Hasher();

  @Before
  public void before() throws IOException {
    MockitoAnnotations.initMocks(this);
    Mockito.doAnswer(
            args -> {
              ((Runnable) args.getArguments()[0]).run();
              return null;
            })
        .when(transactionally)
        .run(Mockito.any(Runnable.class));
  }

  @Test
  public void testTransientExceptionOnStart() throws Exception {
    ScriptJob scriptJob =
        newJob("" + "function start() {\n" + "  throw new Error('Boom!')\n" + "}").build();
    ScriptJobProcessor processor = processor(scriptJob);
    try {
      processor.start();
      fail();
    } catch (Exception e) {
      assertTrue(e.getCause() instanceof ScriptException);
      verify(notificationService)
          .error(
              "Script job 'Test job' failed and will retry: Error: Boom! in <eval> at line number 2 at column number 2",
              e.getCause());
    }
  }

  @Test
  public void testPermanentExceptionOnStart() throws Exception {
    ScriptJob scriptJob =
        newJob("" + "function wrongFunctionName() {\n" + "  throw new Error('Boom!')\n" + "}")
            .build();
    ScriptJobProcessor processor = processor(scriptJob);
    assertEquals(Status.FAILURE_PERMANENT, processor.start());
    verify(notificationService)
        .error(
            Mockito.eq("Script job 'Test job' permanently failed: No such function start"),
            Mockito.any(Exception.class));
  }

  @Test
  public void testSuccessOnStart() throws Exception {
    ScriptJob scriptJob = newJob("" + "function start() {\n" + "  return SUCCESS\n" + "}").build();
    ScriptJobProcessor processor = processor(scriptJob);
    assertEquals(SUCCESS, processor.start());
    processor.stop();
  }

  @Test
  public void testFailOnStart() throws Exception {
    ScriptJob scriptJob =
        newJob("" + "function start() {\n" + "  return FAILURE_PERMANENT\n" + "}").build();
    ScriptJobProcessor processor = processor(scriptJob);
    assertEquals(FAILURE_PERMANENT, processor.start());
    processor.stop();
  }

  @Test
  public void testFailBadlySigned() throws Exception {
    ScriptJob scriptJob =
        ScriptJob.builder()
            .name("Badly signed job")
            .script("" + "function start() {\n" + "  return SUCCESS\n" + "}")
            .scriptHash(
                hasher.hashWithString(
                    ""
                        + "function start() {\n"
                        + "  print('Something nefarious')\n"
                        + "  return SUCCESS\n"
                        + "}",
                    SCRIPT_SIGNING_KEY))
            .build();
    ScriptJobProcessor processor = processor(scriptJob);
    assertEquals(FAILURE_PERMANENT, processor.start());
    processor.stop();
  }

  @Test
  public void testSaveState() throws Exception {
    ScriptJob scriptJob =
        newJob(
                ""
                    + "function start() {\n"
                    + "  state.persistent.set('foo', 'bar')\n"
                    + "  return SUCCESS\n"
                    + "}")
            .build();
    ScriptJobProcessor processor = processor(scriptJob);
    assertEquals(SUCCESS, processor.start());
    verify(jobControl).replace(scriptJob.toBuilder().state(ImmutableMap.of("foo", "bar")).build());
  }

  @Test
  public void testGetState() throws Exception {
    ScriptJob scriptJob =
        newJob(
                ""
                    + "function start() {\n"
                    + "  if (state.persistent.get('foo') !== 'bar') throw new Error('Assertion failed')\n"
                    + "  return SUCCESS\n"
                    + "}")
            .state(ImmutableMap.of("foo", "bar"))
            .build();
    ScriptJobProcessor processor = processor(scriptJob);
    assertEquals(SUCCESS, processor.start());
    processor.stop();
  }

  @Test
  public void testStayResident() throws Exception {
    ScriptJob scriptJob =
        newJob(
                ""
                    + "var interval, count\n"
                    + "function start() {\n"
                    + "  interval = setInterval(poll, 250)\n"
                    + "  count = 1\n"
                    + "  return RUNNING\n"
                    + "}\n"
                    + "function poll() {\n"
                    + "  console.log('Poll')\n"
                    + "  notifications.alert('Alert')\n"
                    + "  notifications.info('Info')\n"
                    + "  notifications.error('Error')\n"
                    + "  if (count >= 3) {\n"
                    + "    control.done()\n"
                    + "    notifications.info('Should not get here')\n"
                    + "  } else {\n"
                    + "    count++\n"
                    + "  }\n"
                    + "}\n"
                    + "function stop() {\n"
                    + "  clearInterval(interval)\n"
                    + "  console.log('Done')\n"
                    + "}")
            .build();
    ScriptJobProcessor processor = Mockito.spy(processor(scriptJob));

    Mockito.doAnswer(
            args -> {
              processor.stop();
              return null;
            })
        .when(jobControl)
        .finish(Mockito.any(Status.class));

    CountDownLatch latch = new CountDownLatch(1);
    Mockito.doAnswer(
            args -> {
              latch.countDown();
              return null;
            })
        .when(processor)
        .dispose(Mockito.any(Disposable.class));

    assertEquals(RUNNING, processor.start());

    assertTrue(latch.await(5, TimeUnit.SECONDS));
    verify(processor)
        .onInterval(Mockito.any(Runnable.class), Mockito.eq(250L), Mockito.anyString());
    verify(notificationService, never()).info("Should not get here");
    verify(notificationService, times(3)).alert("Alert");
    verify(notificationService, times(3)).info("Info");
    verify(notificationService, times(3)).error("Error");
    verify(jobControl).finish(SUCCESS);
    verify(processor).dispose(Mockito.any(Disposable.class));
  }

  @Test
  public void testMonitorTicker() throws Exception {
    ScriptJob scriptJob =
        newJob(
                ""
                    + "var subscription\n"
                    + "function start() {\n"
                    + "  console.log('Pre-subscription', subscription)\n"
                    + "  subscription = events.setTick(onTick, { exchange: 'binance', base: 'BTC', counter: 'USDT' })\n"
                    + "  console.log('Post-subscription', subscription)\n"
                    + "  return RUNNING\n"
                    + "}\n"
                    + "function onTick(event) {\n"
                    + "  try {\n"
                    + "    console.log('Poll')\n"
                    + "    notifications.info(event.toString())\n"
                    + "    trading.limitOrder({\n"
                    + "      market: { exchange: 'binance', base: 'BTC', counter: 'USDT' },\n"
                    + "      direction: BUY,\n"
                    + "      price: decimal('1'),\n"
                    + "      amount: decimal('2')\n"
                    + "    })\n"
                    + "    trading.limitOrder({\n"
                    + "      market: { exchange: 'gdax', base: 'ETH', counter: 'EUR' },\n"
                    + "      direction: SELL,\n"
                    + "      price: '2',\n"
                    + "      amount: '4'\n"
                    + "    })\n"
                    + "    control.done()\n"
                    + "  } catch (err) {\n"
                    + "    // By default, errors are transient. Make it permanent\n"
                    + "    console.log('Error on tick', err)\n"
                    + "    control.fail()\n"
                    + "  }\n"
                    + "}\n"
                    + "function stop() {\n"
                    + "  console.log('Stop')\n"
                    + "  events.clear(subscription)\n"
                    + "}")
            .build();

    ScriptJobProcessor processor = Mockito.spy(processor(scriptJob));

    Mockito.doAnswer(
            args -> {
              processor.stop();
              return null;
            })
        .when(jobControl)
        .finish(Mockito.any(Status.class));

    ExchangeEventSubscription subscription = mock(ExchangeEventSubscription.class);
    Ticker ticker1 = new Ticker.Builder().build();
    Ticker ticker2 = new Ticker.Builder().build();
    TickerSpec spec = TickerSpec.fromKey("binance/USDT/BTC");

    when(exchangeEventRegistry.subscribe(MarketDataSubscription.create(spec, TICKER)))
        .thenReturn(subscription);
    when(subscription.getTickers())
        .thenReturn(
            Flowable.timer(500, TimeUnit.MILLISECONDS)
                .flatMap(
                    x ->
                        Flowable.just(
                            TickerEvent.create(spec, ticker1), TickerEvent.create(spec, ticker2))));

    CountDownLatch latch = new CountDownLatch(1);
    Mockito.doAnswer(
            args -> {
              latch.countDown();
              return null;
            })
        .when(subscription)
        .close();

    assertEquals(RUNNING, processor.start());

    assertTrue(latch.await(5, TimeUnit.SECONDS));

    verify(notificationService).info(TickerEvent.create(spec, ticker1).toString());
    verify(jobSubmitter)
        .submitNewUnchecked(
            LimitOrderJob.builder()
                .direction(Direction.BUY)
                .tickTrigger(TickerSpec.fromKey("binance/USDT/BTC"))
                .amount(new BigDecimal(2))
                .limitPrice(BigDecimal.ONE)
                .build());
    verify(jobSubmitter)
        .submitNewUnchecked(
            LimitOrderJob.builder()
                .direction(Direction.SELL)
                .tickTrigger(TickerSpec.fromKey("gdax/EUR/ETH"))
                .amount(new BigDecimal(4))
                .limitPrice(new BigDecimal(2))
                .build());
    verify(jobControl).finish(SUCCESS);
    verify(subscription).close();
  }

  private ScriptJobProcessor processor(ScriptJob scriptJob) {
    ScriptConfiguration orkoConfiguration =
        new ScriptConfiguration() {
          @Override
          public String getScriptSigningKey() {
            return SCRIPT_SIGNING_KEY;
          }
        };
    return new ScriptJobProcessor(
        scriptJob,
        jobControl,
        exchangeEventRegistry,
        notificationService,
        jobSubmitter,
        transactionally,
        hasher,
        orkoConfiguration);
  }

  private Builder newJob(String script) {
    return ScriptJob.builder()
        .name("Test job")
        .script(script)
        .scriptHash(hasher.hashWithString(script, SCRIPT_SIGNING_KEY));
  }
}
