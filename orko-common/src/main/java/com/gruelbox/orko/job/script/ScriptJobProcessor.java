/**
 * Orko
 * Copyright © 2018-2019 Graham Crockford
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.gruelbox.orko.job.script;

import static com.gruelbox.orko.job.LimitOrderJob.Direction.BUY;
import static com.gruelbox.orko.jobrun.spi.Status.FAILURE_PERMANENT;
import static com.gruelbox.orko.jobrun.spi.Status.RUNNING;
import static com.gruelbox.orko.jobrun.spi.Status.SUCCESS;
import static com.gruelbox.orko.marketdata.MarketDataType.TICKER;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;
import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.gruelbox.orko.OrkoConfiguration;
import com.gruelbox.orko.auth.Hasher;
import com.gruelbox.orko.db.Transactionally;
import com.gruelbox.orko.job.LimitOrderJob;
import com.gruelbox.orko.job.LimitOrderJob.Direction;
import com.gruelbox.orko.jobrun.JobSubmitter;
import com.gruelbox.orko.jobrun.spi.JobControl;
import com.gruelbox.orko.jobrun.spi.Status;
import com.gruelbox.orko.marketdata.ExchangeEventRegistry;
import com.gruelbox.orko.marketdata.ExchangeEventRegistry.ExchangeEventSubscription;
import com.gruelbox.orko.marketdata.MarketDataSubscription;
import com.gruelbox.orko.marketdata.TickerEvent;
import com.gruelbox.orko.notification.NotificationService;
import com.gruelbox.orko.spi.TickerSpec;
import com.gruelbox.orko.util.SafelyClose;
import com.gruelbox.orko.util.SafelyDispose;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import jdk.nashorn.api.scripting.JSObject;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import jdk.nashorn.internal.runtime.ParserException;

/**
 * Processor for {@link ScriptJob}.
 *
 * @author Graham Crockford
 */
class ScriptJobProcessor implements ScriptJob.Processor {

  private static final Logger LOGGER = LoggerFactory.getLogger(ScriptJobProcessor.class);

  private final JobControl jobControl;
  private ScriptEngine engine;

  private final ExchangeEventRegistry exchangeEventRegistry;
  private final NotificationService notificationService;
  private final JobSubmitter jobSubmitter;
  private final Transactionally transactionally;
  private final Hasher hasher;
  private final OrkoConfiguration configuration;

  private volatile ScriptJob job;
  private volatile boolean done;

  @AssistedInject
  public ScriptJobProcessor(@Assisted ScriptJob job,
                            @Assisted JobControl jobControl,
                            ExchangeEventRegistry exchangeEventRegistry,
                            NotificationService notificationService,
                            JobSubmitter jobSubmitter,
                            Transactionally transactionally,
                            Hasher hasher,
                            OrkoConfiguration configuration) {
    this.job = job;
    this.jobControl = jobControl;
    this.exchangeEventRegistry = exchangeEventRegistry;
    this.notificationService = notificationService;
    this.jobSubmitter = jobSubmitter;
    this.transactionally = transactionally;
    this.hasher = hasher;
    this.configuration = configuration;
  }

  @Override
  public Status start() {
    String hash = hasher.hashWithString(job.script(), configuration.getScriptSigningKey());
    if (!hash.equals(job.scriptHash())) {
      notifyAndLogError("Script job '" + job.name() + "' has invalid hash. Failed permanently");
      return Status.FAILURE_PERMANENT;
    }
    try {
      initialiseEngine();
    } catch (Exception e) {
      notificationService.error("Script job '" + job.name() + "' permanently failed: " + e.getMessage(), e);
      LOGGER.error("Failed script:\n" + job.script());
      return Status.FAILURE_PERMANENT;
    }
    try {
      Invocable invocable = (Invocable) engine;
      return (Status) invocable.invokeFunction("start");
    } catch (NoSuchMethodException e) {
      notificationService.error("Script job '" + job.name() + "' permanently failed: " + e.getMessage(), e);
      LOGGER.error("Failed script:\n" + job.script());
      return Status.FAILURE_PERMANENT;
    } catch (Exception e) {
      notifyAndLogError("Script job '" + job.name() + "' failed and will retry: " + e.getMessage(), e);
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  @Override
  public void setReplacedJob(ScriptJob job) {
    this.job = job;
  }

  @Override
  public void stop() {
    if (engine == null)
      return;
    Invocable invocable = (Invocable) engine;
    try {
      invocable.invokeFunction("stop");
    } catch (NoSuchMethodException e) {
      // Fine
    } catch (Exception e) {
      Throwables.throwIfUnchecked(e);
      throw new RuntimeException(e);
    }
  }

  private void initialiseEngine() throws ScriptException, ParserException {
    engine = new NashornScriptEngineFactory().getScriptEngine(new String[] { "--no-java" });
    createBindings();
    engine.eval(job.script());
  }

  private void createBindings() {
    Bindings bindings = engine.createBindings();
    Events events = new Events();

    bindings.put("SUCCESS", SUCCESS);
    bindings.put("FAILURE_PERMANENT", FAILURE_PERMANENT);
    bindings.put("RUNNING", RUNNING);
    bindings.put("BUY", BUY);
    bindings.put("SELL", Direction.SELL);

    bindings.put("notifications", notificationService);
    bindings.put("events", events);
    bindings.put("control", new Control());
    bindings.put("console", new Console());
    bindings.put("trading", new Trading());
    bindings.put("state", new State());

    bindings.put("decimal", new Function<String, BigDecimal>() {
      @Override
      public BigDecimal apply(String value) {
        return new BigDecimal(value);
      }
    });

    bindings.put("setInterval", new BiFunction<JSObject, Integer, Disposable>() {
      @Override
      public Disposable apply(JSObject callback, Integer timeout) {
        return events.setInterval(callback, timeout);
      }
    });

    bindings.put("clearInterval", new Consumer<Disposable>() {
      @Override
      public void accept(Disposable disposable) {
        events.clear(disposable);
      }
    });

    engine.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
  }

  private static final class PermanentFailureException extends RuntimeException {
    private static final long serialVersionUID = 5862312296152854315L;
    PermanentFailureException() {
      super();
    }
  }

  private static final class TransientFailureException extends RuntimeException {
    private static final long serialVersionUID = -7935634777240490608L;
    TransientFailureException() {
      super();
    }
  }

  public final class Console {

    public void log(Object o) {
      LOGGER.info("{} - {}", job.name(), o);
    }

    public void log(Object o, Exception error) {
      LOGGER.error(job.name() + " - " + o, error);
    }

    public void log(Object o, Object value) {
      LOGGER.info("{} - {}, {}", job.name(), o, value);
    }

    @Override
    public String toString() {
      return "console";
    }

  }

  public final class Control {

    public void fail() {
      throw new PermanentFailureException();
    }

    public void restart() {
      throw new TransientFailureException();
    }

    public void done() {
      jobControl.finish(SUCCESS);
    }

    @Override
    public String toString() {
      return "control";
    }

  }

  public final class Events {

    private final AtomicBoolean failing = new AtomicBoolean(false);

    public Disposable setTick(JSObject callback, JSObject tickerSpec) {

      return onTick(
        event -> {
          synchronized(ScriptJobProcessor.this) {
            if (done)
              return;
            try {
              transactionally.run(() -> callback.call(null, event));
              successfulPoll();
            } catch (PermanentFailureException e) {
              notifyAndLogError("Script job '" + job.name() + "' failed permanently: " + e.getMessage(), e);
              jobControl.finish(FAILURE_PERMANENT);
            } catch (Exception e) {
              failingPoll(e);
            }
          }
        },
        convertTickerSpec(tickerSpec),
        callback.toString()
      );
    }

    public Disposable setInterval(JSObject callback, Integer timeout) {
      return onInterval(
        () -> {
          synchronized(ScriptJobProcessor.this) {
            if (done)
              return;
            try {
              transactionally.run(() -> callback.call(null));
              successfulPoll();
            } catch (PermanentFailureException e) {
              notifyAndLogError("Script job '" + job.name() + "' failed permanently: " + e.getMessage(), e);
              jobControl.finish(FAILURE_PERMANENT);
            } catch (Exception e) {
              failingPoll(e);
            }
          }
        },
        timeout,
        callback.toString()
      );
    }

    private void successfulPoll() {
      if (failing.compareAndSet(true, false)) {
        notificationService.alert("Script job '" + job.name() + "' working again");
      }
    }

    private void failingPoll(Exception e) {
      if (failing.compareAndSet(false, true)) {
        notifyAndLogError("Script job '" + job.name() + "' failing: " + e.getMessage(), e);
      } else {
        LOGGER.error("Script job '" + job.name() + "' failed again: " + e.getMessage());
      }
    }

    public void clear(Disposable disposable) {
      dispose(disposable);
    }

    @Override
    public String toString() {
      return "events";
    }

  }

  public final class State {

    public final StateManager<String> persistent = new StateManager<String>() {

      @Override
      public void set(String key, String value) {
        HashMap<String, String> newState = new HashMap<>();
        newState.putAll(job.state());
        newState.put(key, value);
        jobControl.replace(job.toBuilder().state(newState).build());
      }

      @Override
      public String get(String key) {
        return job.state().get(key);
      }

      @Override
      public void remove(String key) {
        HashMap<String, String> newState = new HashMap<>();
        newState.putAll(job.state());
        newState.remove(key);
        jobControl.replace(job.toBuilder().state(newState).build());
      }

      @Override
      public String toString() {
        return job.state().toString();
      }

      @Override
      public void increment(String key) {
        String value = get(key);
        try {
          long asLong = Long.parseLong(value);
          set(key, Long.toString(asLong + 1));
        } catch (NumberFormatException e) {
          throw new IllegalStateException(key + " is not a precise numeric value, so cannot be incremented");
        }
      }
    };

    @Override
    public String toString() {
      return "state";
    }
  }


  public final class Trading {

    public void limitOrder(JSObject request) {
      TickerSpec spec = convertTickerSpec((JSObject) request.getMember("market"));
      Direction direction = (Direction) request.getMember("direction");
      BigDecimal price =  (BigDecimal) request.getMember("price");
      BigDecimal amount =  (BigDecimal) request.getMember("amount");
      LOGGER.info("Script job '{}' Submitting limit order: {} {} {} on {} at {}",
          job.name(), direction, amount, spec.base(), spec, price);
      jobSubmitter.submitNewUnchecked(
        LimitOrderJob.builder()
          .direction(direction)
          .tickTrigger(spec)
          .amount(amount)
          .limitPrice(price)
          .build()
      );
    }

    @Override
    public String toString() {
      return "trading";
    }

  }


  public interface StateManager<T> {
    public T get(String key);
    public void set(String key, T value);
    public void remove(String key);
    public void increment(String key);
  }

  Disposable onInterval(Runnable runnable, long timeout, String description) {
    Disposable result = Observable.interval(timeout, MILLISECONDS).subscribe(x -> runnable.run());
    return new Disposable() {

      @Override
      public boolean isDisposed() {
        return result.isDisposed();
      }

      @Override
      public void dispose() {
        SafelyDispose.of(result);
      }

      @Override
      public String toString() {
        return description;
      }
    };
  }

  void dispose(Disposable disposable) {
    SafelyDispose.of(disposable);
  }

  private TickerSpec convertTickerSpec(JSObject tickerSpec) {
    return TickerSpec.builder()
      .exchange((String) tickerSpec.getMember("exchange"))
      .base((String) tickerSpec.getMember("base"))
      .counter((String) tickerSpec.getMember("counter"))
      .build();
  }

  public Disposable onTick(io.reactivex.functions.Consumer<TickerEvent> handler, TickerSpec tickerSpec, String description) {
    ExchangeEventSubscription subscription = exchangeEventRegistry.subscribe(MarketDataSubscription.create(tickerSpec, TICKER));
    Disposable disposable = subscription.getTickers().subscribe(handler);

    return new Disposable() {

      @Override
      public boolean isDisposed() {
        return disposable.isDisposed();
      }

      @Override
      public void dispose() {
        SafelyDispose.of(disposable);
        SafelyClose.the(subscription);
      }

      @Override
      public String toString() {
        return description;
      }
    };
  }

  private void notifyAndLogError(String message) {
    notificationService.error(message);
  }

  private void notifyAndLogError(String message, Throwable t) {
    notificationService.error(message, t);
  }

  public static final class Module extends AbstractModule {
    @Override
    protected void configure() {
      install(new FactoryModuleBuilder()
          .implement(ScriptJob.Processor.class, ScriptJobProcessor.class)
          .build(ScriptJob.Processor.ProcessorFactory.class));
    }
  }
}
