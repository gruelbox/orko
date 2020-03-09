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
package com.gruelbox.orko.db;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.util.Providers;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.hibernate.UnitOfWork;
import io.dropwizard.hibernate.UnitOfWorkAspect;
import java.lang.annotation.Annotation;
import java.util.concurrent.Callable;
import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.SessionFactory;
import org.hibernate.context.internal.ManagedSessionContext;

@Singleton
public class Transactionally {

  public static final UnitOfWork DEFAULT_UNIT =
      new UnitOfWork() {

        @Override
        public Class<? extends Annotation> annotationType() {
          return UnitOfWork.class;
        }

        @Override
        public String value() {
          return HibernateBundle.DEFAULT_NAME;
        }

        @Override
        public boolean transactional() {
          return true;
        }

        @Override
        public boolean readOnly() {
          return false;
        }

        @Override
        public FlushMode flushMode() {
          return FlushMode.AUTO;
        }

        @Override
        public CacheMode cacheMode() {
          return CacheMode.NORMAL;
        }
      };

  public static final UnitOfWork READ_ONLY_UNIT =
      new UnitOfWork() {

        @Override
        public Class<? extends Annotation> annotationType() {
          return UnitOfWork.class;
        }

        @Override
        public String value() {
          return HibernateBundle.DEFAULT_NAME;
        }

        @Override
        public boolean transactional() {
          return true;
        }

        @Override
        public boolean readOnly() {
          return false;
        }

        @Override
        public FlushMode flushMode() {
          return FlushMode.AUTO;
        }

        @Override
        public CacheMode cacheMode() {
          return CacheMode.NORMAL;
        }
      };

  private final Provider<SessionFactory> sessionFactory;
  private final boolean allowNested;

  @Inject
  Transactionally(Provider<SessionFactory> sessionFactory) {
    this(sessionFactory, false);
  }

  @VisibleForTesting
  public Transactionally(SessionFactory sessionFactory) {
    this(Providers.of(sessionFactory), false);
  }

  private Transactionally(Provider<SessionFactory> sessionFactory, boolean allowNested) {
    this.sessionFactory = sessionFactory;
    this.allowNested = allowNested;
  }

  public Transactionally allowingNested() {
    return new Transactionally(sessionFactory, true);
  }

  public void run(UnitOfWork unitOfWork, Runnable runnable) {
    call(
        unitOfWork,
        () -> {
          runnable.run();
          return null;
        });
  }

  public void run(Runnable runnable) {
    call(
        () -> {
          runnable.run();
          return null;
        });
  }

  public <T> T call(Callable<T> callable) {
    return call(DEFAULT_UNIT, callable);
  }

  public <T> T callChecked(Callable<T> callable) throws Exception {
    return callChecked(DEFAULT_UNIT, callable);
  }

  public <T> T callChecked(UnitOfWork unitOfWork, Callable<T> callable) throws Exception {
    boolean nested = ManagedSessionContext.hasBind(sessionFactory.get());
    if (nested) {
      if (allowNested) {
        return callable.call();
      } else {
        throw new IllegalStateException("Nested units of work not permitted");
      }
    }
    UnitOfWorkAspect unitOfWorkAspect =
        new UnitOfWorkAspect(ImmutableMap.of(HibernateBundle.DEFAULT_NAME, sessionFactory.get()));
    try {
      unitOfWorkAspect.beforeStart(unitOfWork);
      T result = callable.call();
      unitOfWorkAspect.afterEnd();
      return result;
    } catch (Exception e) {
      unitOfWorkAspect.onError();
      throw e;
    } finally {
      unitOfWorkAspect.onFinish();
    }
  }

  public <T> T call(UnitOfWork unitOfWork, Callable<T> callable) {
    try {
      return callChecked(unitOfWork, callable);
    } catch (Exception e) {
      Throwables.throwIfUnchecked(e);
      throw new RuntimeException(e);
    }
  }
}
