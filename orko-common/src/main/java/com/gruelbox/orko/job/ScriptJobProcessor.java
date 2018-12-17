package com.gruelbox.orko.job;

import static com.gruelbox.orko.jobrun.spi.Status.FAILURE_PERMANENT;
import static com.gruelbox.orko.jobrun.spi.Status.RUNNING;
import static com.gruelbox.orko.jobrun.spi.Status.SUCCESS;
import static com.gruelbox.orko.marketdata.MarketDataType.TICKER;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
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
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import jdk.nashorn.api.scripting.ScriptObjectMirror;

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
  private final Map<String, Object> transientState = new HashMap<>();

  @AssistedInject
  public ScriptJobProcessor(@Assisted ScriptJob job,
                            @Assisted JobControl jobControl,
                            ExchangeEventRegistry exchangeEventRegistry,
                            NotificationService notificationService) {
    this.job = job;
    this.jobControl = jobControl;
    this.exchangeEventRegistry = exchangeEventRegistry;
    this.notificationService = notificationService;
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
    bindings.put("SUCCESS", SUCCESS);
    bindings.put("FAILURE_PERMANENT", FAILURE_PERMANENT);
    bindings.put("RUNNING", RUNNING);
    bindings.put("transientState", transientState);
    bindings.put("jobControl", jobControl);
    bindings.put("notifications", notificationService);
    bindings.put("setInterval", new BiFunction<ScriptObjectMirror, Integer, Disposable>() {
      @Override
      public Disposable apply(ScriptObjectMirror callback, Integer timeout) {
        return setInterval(() -> {
          synchronized(ScriptJobProcessor.this) {
            callback.call(null);
          }
        }, timeout);
      }
    });
    bindings.put("setTick", new BiFunction<ScriptObjectMirror, ScriptObjectMirror, Disposable>() {
      @Override
      public Disposable apply(ScriptObjectMirror callback, ScriptObjectMirror tickerSpec) {
        return onTick(
          event -> {
            synchronized(ScriptJobProcessor.this) {
              callback.call(null, event);
            }
          },
          TickerSpec.builder()
            .exchange((String) tickerSpec.get("exchange"))
            .base((String) tickerSpec.get("base"))
            .counter((String) tickerSpec.get("counter"))
            .build()
        );
      }
    });
    bindings.put("getSavedState", new Function<String, String>() {
      @Override
      public String apply(String key) {
        return job.state().get(key);
      }
    });
    bindings.put("saveState", new BiConsumer<String, String>() {
      @Override
      public void accept(String key, String value) {
        HashMap<String, String> newState = new HashMap<>();
        newState.putAll(job.state());
        newState.put(key, value);
        jobControl.replace(job.toBuilder().state(newState).build());
      }
    });
    bindings.put("clearSavedState", new Consumer<String>() {
      @Override
      public void accept(String key) {
        HashMap<String, String> newState = new HashMap<>();
        newState.putAll(job.state());
        newState.remove(key);
        jobControl.replace(job.toBuilder().state(newState).build());
      }
    });
    bindings.put("log", new Consumer<Object>() {
      @Override
      public void accept(Object string) {
        LOGGER.info("{} - {}", job.name(), string);
      }
    });
    bindings.put("clearInterval", new Consumer<Disposable>() {
      @Override
      public void accept(Disposable disposable) {
        dispose(disposable);
      }
    });
    bindings.put("clearTick", new Consumer<Disposable>() {
      @Override
      public void accept(Disposable disposable) {
        dispose(disposable);
      }
    });
    engine.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
  }

  Disposable setInterval(Runnable runnable, long timeout) {
    return Observable.interval(timeout, MILLISECONDS)
        .subscribe(x -> runnable.run());
  }

  void dispose(Disposable disposable) {
    SafelyDispose.of(disposable);
  }

  public Disposable onTick(io.reactivex.functions.Consumer<TickerEvent> handler, TickerSpec tickerSpec) {
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