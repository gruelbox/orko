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
package com.gruelbox.orko.exchange;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import java.util.concurrent.atomic.AtomicReference;

class PersistentPublisher<T> {
  private final Flowable<T> flowable;
  private final AtomicReference<FlowableEmitter<T>> emitter = new AtomicReference<>();

  PersistentPublisher() {
    this.flowable =
        setup(
                Flowable.create(
                    (FlowableEmitter<T> e) -> emitter.set(e.serialize()),
                    BackpressureStrategy.MISSING))
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
    if (emitter.get() != null) emitter.get().onNext(e);
  }
}
