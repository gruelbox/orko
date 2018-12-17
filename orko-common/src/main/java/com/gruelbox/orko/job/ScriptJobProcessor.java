package com.gruelbox.orko.job;

import static com.gruelbox.orko.job.LimitOrderJob.Direction.BUY;
import static com.gruelbox.orko.jobrun.spi.Status.FAILURE_PERMANENT;
import static com.gruelbox.orko.jobrun.spi.Status.RUNNING;
import static com.gruelbox.orko.jobrun.spi.Status.SUCCESS;
import static com.gruelbox.orko.marketdata.MarketDataType.TICKER;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
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

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.gruelbox.orko.db.Transactionally;
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

/**
 * Processor for {@link ScriptJob}.
 *
 * @author Graham Crockford
 */
class ScriptJobProcessor implements ScriptJob.Processor {

  private static final Logger LOGGER = LoggerFactory.getLogger(ScriptJobProcessor.class);

  private final ScriptJob job;
  private final JobControl jobControl;
  private ScriptEngine engine;

  private final ExchangeEventRegistry exchangeEventRegistry;
  private final NotificationService notificationService;
  private final JobSubmitter jobSubmitter;
  private final Transactionally transactionally;

  private final Map<String, Object> transientState = new HashMap<>();
  private volatile boolean done;

  @AssistedInject
  public ScriptJobProcessor(@Assisted ScriptJob job,
                            @Assisted JobControl jobControl,
                            ExchangeEventRegistry exchangeEventRegistry,
                            NotificationService notificationService,
                            JobSubmitter jobSubmitter,
                            Transactionally transactionally) {
    this.job = job;
    this.jobControl = jobControl;
    this.exchangeEventRegistry = exchangeEventRegistry;
    this.notificationService = notificationService;
    this.jobSubmitter = jobSubmitter;
    this.transactionally = transactionally;
  }

  @Override
  public Status start() {
    initialiseEngine();
    Invocable invocable = (Invocable) engine;
    try {
      return (Status) invocable.invokeFunction("start");
    } catch (ScriptException e) {
      notificationService.error("Script job '" + job.name() + "' failed and will retry: " + e.getMessage(), e);
      throw new RuntimeException(e.getMessage(), e);
    } catch (NoSuchMethodException e) {
      notificationService.error("Script job '" + job.name() + "' permanently failed: " + e.getMessage(), e);
      return Status.FAILURE_PERMANENT;
    }
  }

  @Override
  public void stop() {
    Invocable invocable = (Invocable) engine;
    try {
      invocable.invokeFunction("stop");
    } catch (ScriptException e) {
      throw new RuntimeException(e);
    } catch (NoSuchMethodException e) {
      // Fine
    }
  }

  private void initialiseEngine() {
    engine = new NashornScriptEngineFactory().getScriptEngine(new String[] { "--no-java" });
    createBindings();
    try {
      engine.eval(job.script());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
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

  }

  public final class Events {

    public Disposable setTick(JSObject callback, JSObject tickerSpec) {
      return onTick(
        event -> {
          synchronized(ScriptJobProcessor.this) {
            if (done)
              return;
            try {
              transactionally.run(() -> callback.call(null, event));
            } catch (PermanentFailureException e) {
              jobControl.finish(FAILURE_PERMANENT);
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
            } catch (PermanentFailureException e) {
              jobControl.finish(FAILURE_PERMANENT);
            }
          }
        },
        timeout,
        callback.toString()
      );
    }

    public void clear(Disposable disposable) {
      dispose(disposable);
    }

  }

  public final class State {

    public final StateManager<Object> local = new StateManager<Object>() {

      @Override
      public void set(String key, Object value) {
        transientState.put(key, value);
      }

      @Override
      public Object get(String key) {
        return transientState.get(key);
      }

      @Override
      public void remove(String key) {
        transientState.remove(key);
      }

      @Override
      public String toString() {
        return transientState.toString();
      }

      @Override
      public void increment(String key) {
        Object value = get(key);
        if (value instanceof Integer) {
          set(key, ((Integer)value) + 1);
        } else if (value instanceof Long) {
          set(key, ((Long)value) + 1);
        } else if (value instanceof BigDecimal) {
          set(key, ((BigDecimal)value).add(BigDecimal.ONE));
        } else {
          throw new IllegalStateException(key + " is a " + key.getClass().getName() + ", not an precise numeric value, so cannot be incremented");
        }
      }
    };

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

  public static final class Module extends AbstractModule {
    @Override
    protected void configure() {
      install(new FactoryModuleBuilder()
          .implement(ScriptJob.Processor.class, ScriptJobProcessor.class)
          .build(ScriptJob.Processor.ProcessorFactory.class));
    }
  }
}