package com.gruelbox.orko.db;

import java.lang.annotation.Annotation;
import java.util.concurrent.Callable;

import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.SessionFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.util.Providers;

import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.hibernate.UnitOfWork;
import io.dropwizard.hibernate.UnitOfWorkAspect;
import jersey.repackaged.com.google.common.collect.ImmutableMap;

@Singleton
public class Transactionally {

  public static final UnitOfWork DEFAULT_UNIT = new UnitOfWork() {

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

  public static final UnitOfWork READ_ONLY_UNIT = new UnitOfWork() {

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

  @Inject
  Transactionally(Provider<SessionFactory> sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @VisibleForTesting
  public Transactionally(SessionFactory sessionFactory) {
    this.sessionFactory = Providers.of(sessionFactory);
  }

  public void run(Runnable runnable) {
    call(() -> {
      runnable.run();
      return null;
    });
  }

  public <T> T call(Callable<T> callable) {
    return call(DEFAULT_UNIT, callable);
  }

  public <T> T call(UnitOfWork unitOfWork, Callable<T> callable) {
    UnitOfWorkAspect unitOfWorkAspect = new UnitOfWorkAspect(ImmutableMap.of(HibernateBundle.DEFAULT_NAME, sessionFactory.get()));
    try {
      unitOfWorkAspect.beforeStart(unitOfWork);
      T result = callable.call();
      unitOfWorkAspect.afterEnd();
      return result;
    } catch (Exception e) {
      unitOfWorkAspect.onError();
      Throwables.throwIfUnchecked(e);
      throw new RuntimeException(e);
    } finally {
      unitOfWorkAspect.onFinish();
    }
  }
}