package com.gruelbox.orko.job;

import static com.gruelbox.orko.jobrun.spi.Status.FAILURE_PERMANENT;
import static com.gruelbox.orko.jobrun.spi.Status.RUNNING;
import static com.gruelbox.orko.jobrun.spi.Status.SUCCESS;
import static com.gruelbox.orko.marketdata.MarketDataType.TICKER;
import static java.lang.Thread.sleep;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.script.ScriptException;

import org.junit.Before;
import org.junit.Test;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.ImmutableMap;
import com.gruelbox.orko.db.Transactionally;
import com.gruelbox.orko.job.ScriptJob.Builder;
import com.gruelbox.orko.jobrun.spi.JobControl;
import com.gruelbox.orko.jobrun.spi.Status;
import com.gruelbox.orko.marketdata.ExchangeEventRegistry;
import com.gruelbox.orko.marketdata.ExchangeEventRegistry.ExchangeEventSubscription;
import com.gruelbox.orko.marketdata.MarketDataSubscription;
import com.gruelbox.orko.marketdata.TickerEvent;
import com.gruelbox.orko.notification.NotificationService;
import com.gruelbox.orko.spi.TickerSpec;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;

public class TestScriptJobProcessor {

  @Mock private JobControl jobControl;
  @Mock private ExchangeEventRegistry exchangeEventRegistry;
  @Mock private NotificationService notificationService;
  @Mock private Transactionally transactionally;

  @Before
  public void before() throws IOException {
    MockitoAnnotations.initMocks(this);
    Mockito.doAnswer(args -> {
      ((Runnable) args.getArguments()[0]).run();
      return null;
    }).when(transactionally).run(Mockito.any(Runnable.class));
  }

  /* -------------------------------------------------------------------------------------- */

  @Test
  public void testTransientExceptionOnStart() throws Exception {
    ScriptJob scriptJob = newJob().script(""
        + "function start() {\n"
        + "  throw new Error('Boom!')\n"
        + "}").build();
    ScriptJobProcessor processor = processor(scriptJob);
    try {
      processor.start();
      fail();
    } catch (Exception e) {
      assertTrue(e.getCause() instanceof ScriptException);
      verify(notificationService).error("Script job 'Test job' failed and will retry: Error: Boom! in <eval> at line number 2 at column number 2", e.getCause());
    }
  }

  @Test
  public void testPermanentExceptionOnStart() throws Exception {
    ScriptJob scriptJob = newJob().script(""
        + "function wrongFunctionName() {\n"
        + "  throw new Error('Boom!')\n"
        + "}").build();
    ScriptJobProcessor processor = processor(scriptJob);
    assertEquals(Status.FAILURE_PERMANENT, processor.start());
    verify(notificationService).error(Mockito.eq("Script job 'Test job' permanently failed: No such function start"), Mockito.any(Exception.class));
  }

  @Test
  public void testSuccessOnStart() throws Exception {
    ScriptJob scriptJob = newJob().script(""
        + "function start() {\n"
        + "  return SUCCESS\n"
        + "}").build();
    ScriptJobProcessor processor = processor(scriptJob);
    assertEquals(SUCCESS, processor.start());
    processor.stop();
  }

  @Test
  public void testFailOnStart() throws Exception {
    ScriptJob scriptJob = newJob().script(""
        + "function start() {\n"
        + "  return FAILURE_PERMANENT\n"
        + "}").build();
    ScriptJobProcessor processor = processor(scriptJob);
    assertEquals(FAILURE_PERMANENT, processor.start());
    processor.stop();
  }

  @Test
  public void testSaveState() throws Exception {
    ScriptJob scriptJob = newJob().script(""
        + "function start() {\n"
        + "  saveState('foo', 'bar')\n"
        + "  return SUCCESS\n"
        + "}").build();
    ScriptJobProcessor processor = processor(scriptJob);
    assertEquals(SUCCESS, processor.start());
    verify(jobControl)
      .replace(scriptJob.toBuilder().state(ImmutableMap.of("foo", "bar")).build());
  }

  @Test
  public void testGetState() throws Exception {
    ScriptJob scriptJob = newJob().state(ImmutableMap.of("foo", "bar")).script(""
        + "function start() {\n"
        + "  if (getSavedState('foo') !== 'bar') throw new Error('Assertion failed')\n"
        + "  return SUCCESS\n"
        + "}").build();
    ScriptJobProcessor processor = processor(scriptJob);
    assertEquals(SUCCESS, processor.start());
    processor.stop();
  }

  @Test
  public void testStayResident() throws Exception {
    ScriptJob scriptJob = newJob().script(""
        + "function start() {\n"
        + "  transientState.interval = setInterval(poll, 1000)\n"
        + "  return RUNNING\n"
        + "}\n"
        + "function poll() {\n"
        + "  print('Poll')\n"
        + "  notifications.alert('Alert')\n"
        + "  notifications.info('Info')\n"
        + "  notifications.error('Error')\n"
        + "  jobControl.finish(SUCCESS)\n"
        + "}\n"
        + "function stop() {\n"
        + "  clearInterval(transientState.interval)\n"
        + "  print('Done')\n"
        + "}").build();
    ScriptJobProcessor processor = Mockito.spy(processor(scriptJob));

    Mockito.doAnswer(args -> {
      processor.stop();
      return null;
    }).when(jobControl).finish(SUCCESS);

    assertEquals(RUNNING, processor.start());
    sleep(3000);
    InOrder inOrder = Mockito.inOrder(jobControl, processor, notificationService);
    inOrder.verify(processor).setInterval(Mockito.any(Runnable.class), Mockito.eq(1000L));
    inOrder.verify(notificationService).alert("Alert");
    inOrder.verify(notificationService).info("Info");
    inOrder.verify(notificationService).error("Error");
    inOrder.verify(jobControl).finish(SUCCESS);
    inOrder.verify(processor).dispose(Mockito.any(Disposable.class));
  }

  @Test
  public void testMonitorTicker() throws Exception {
    ScriptJob scriptJob = newJob().script(""
        + "function start() {\n"
        + "  log('x' + transientState)\n"
        + "  transientState.subscription = setTick(onTick, { exchange: 'binance', base: 'BTC', counter: 'USDT' })\n"
        + "  log('y' + transientState)\n"
        + "  return RUNNING\n"
        + "}\n"
        + "function onTick(event) {\n"
        + "  try {\n"
        + "    log('Poll')\n"
        + "    notifications.info(event.toString())\n"
        + "    jobControl.finish(SUCCESS)\n"
        + "  } catch (err) {\n"
        + "    log('Error on tick: ' + err)\n"
        + "    jobControl.finish(FAILURE_PERMANENT)\n"
        + "  }\n"
        + "}\n"
        + "function stop() {\n"
        + "  log('Stop')\n"
        + "  clearTick(transientState.subscription)\n"
        + "}").build();

    ScriptJobProcessor processor = Mockito.spy(processor(scriptJob));

    Mockito.doAnswer(args -> {
      processor.stop();
      return null;
    }).when(jobControl).finish(Mockito.any(Status.class));

    ExchangeEventSubscription subscription = mock(ExchangeEventSubscription.class);
    Ticker ticker1 = new Ticker.Builder().build();
    Ticker ticker2 = new Ticker.Builder().build();
    TickerSpec spec = TickerSpec.fromKey("binance/USDT/BTC");

    when(exchangeEventRegistry.subscribe(MarketDataSubscription.create(spec, TICKER)))
      .thenReturn(subscription);
    when(subscription.getTickers())
      .thenReturn(
        Flowable.timer(500, TimeUnit.MILLISECONDS)
          .flatMap(x ->
            Flowable.just(
              TickerEvent.create(spec, ticker1),
              TickerEvent.create(spec, ticker2)
            )
          )
      );

    assertEquals(RUNNING, processor.start());

    Thread.sleep(1000);

    verify(jobControl, Mockito.atLeastOnce()).finish(SUCCESS);
    verify(notificationService, Mockito.atLeastOnce()).info(TickerEvent.create(spec, ticker1).toString());
    verify(subscription).close();
  }

  private ScriptJobProcessor processor(ScriptJob scriptJob) {
    return new ScriptJobProcessor(scriptJob, jobControl,
        exchangeEventRegistry, notificationService, transactionally);
  }

  private Builder newJob() {
    return ScriptJob.builder().name("Test job");
  }
}