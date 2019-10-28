package com.gruelbox.orko.exchange;

import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;

class PersistentPublisher<T> {
  private final Flowable<T> flowable;
  private final AtomicReference<FlowableEmitter<T>> emitter = new AtomicReference<>();

  PersistentPublisher() {
    this.flowable = setup(Flowable.create((FlowableEmitter<T> e) -> emitter.set(e.serialize()), BackpressureStrategy.MISSING))
        .share()
        .onBackpressureLatest();
  }

  Flowable<T> setup(Flowable<T> base) {
    return base;
  }

  Flowable<T> getAll() {
    return flowable;
  }

  final void emit(T e) {
    if (emitter.get() != null)
      emitter.get().onNext(e);
  }
}
