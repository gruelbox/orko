package com.gruelbox.orko.exchange;

import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.Service.Listener;
import com.google.common.util.concurrent.Service.State;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.schedulers.Schedulers;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class RxServiceWrapper<T extends Service> {

  private final T service;

  private final BlockingQueue<CompletableEmitter> statusEmitters = new ArrayBlockingQueue<>(1);

  public RxServiceWrapper(T service) {
    this.service = service;
    service.addListener(new Listener() {
      @Override
      public void running() {
        Optional.ofNullable(statusEmitters.poll())
            .ifPresent(CompletableEmitter::onComplete);
      }

      @Override
      public void terminated(State from) {
        Optional.ofNullable(statusEmitters.poll())
            .ifPresent(CompletableEmitter::onComplete);
      }

      @Override
      public void failed(State from, Throwable failure) {
        Optional.ofNullable(statusEmitters.poll())
            .ifPresent(emitter -> emitter.onError(failure));
      }
    }, Schedulers.computation()::scheduleDirect);
  }

  public Completable start() {
    return Completable.create(emitter -> {
      statusEmitters.put(emitter); // Implicit block as 1-length queue, for safety
      if (isRunning()) {
        emitter.onComplete();
        statusEmitters.clear();
      } else {
        service.startAsync();
      }
    });
  }

  public Completable stop() {
    return Completable.create(emitter -> {
      statusEmitters.put(emitter); // Implicit block as 1-length queue, for safety
      if (!isRunning()) {
        emitter.onComplete();
        statusEmitters.clear();
      } else {
        service.stopAsync();
      }
    });
  }

  public boolean isRunning() {
    return service.isRunning();
  }

  public State state() {
    return service.state();
  }

  public Throwable failureCause() {
    return service.failureCause();
  }

  T getDelegate() {
    return (T) service;
  }

}
