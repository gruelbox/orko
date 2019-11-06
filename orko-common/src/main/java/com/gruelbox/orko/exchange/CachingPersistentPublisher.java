package com.gruelbox.orko.exchange;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import com.google.common.collect.Maps;

import io.reactivex.Flowable;

final class CachingPersistentPublisher<T, U> extends PersistentPublisher<T> {
  private final ConcurrentMap<U, T> latest = Maps.newConcurrentMap();
  private final Function<T, U> keyFunction;
  private Function<Iterable<T>, Iterable<T>> initialSnapshotSortFunction;

  CachingPersistentPublisher(Function<T, U> keyFunction) {
    super();
    this.keyFunction = keyFunction;
  }

  @Override
  Flowable<T> setup(Flowable<T> base) {
    return base.doOnNext(e -> latest.put(this.keyFunction.apply(e), e));
  }

  void removeFromCache(U key) {
    latest.remove(key);
  }

  void removeFromCache(Predicate<T> matcher) {
    Set<U> removals = new HashSet<>();
    latest.entrySet().stream()
      .filter(e -> matcher.test(e.getValue()))
      .map(Map.Entry::getKey)
      .forEach(removals::add);
    removals.forEach(latest::remove);
  }

  public CachingPersistentPublisher<T, U> orderInitialSnapshotBy(UnaryOperator<Iterable<T>> ordering) {
    this.initialSnapshotSortFunction = ordering;
    return this;
  }

  @Override
  Flowable<T> getAll() {
    if (initialSnapshotSortFunction == null) {
      return super.getAll().startWith(Flowable.defer(() -> Flowable.fromIterable(latest.values())));
    } else {
      return super.getAll().startWith(Flowable.defer(() -> Flowable.fromIterable(initialSnapshotSortFunction.apply(latest.values()))));
    }
  }
}
