package com.gruelbox.orko.db;

import java.util.concurrent.Callable;

import org.mockito.Mockito;

import io.dropwizard.hibernate.UnitOfWork;

public class MockTransactionallyFactory {

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static Transactionally mockTransactionally() {
    Transactionally mock = Mockito.mock(Transactionally.class);
    Mockito.doAnswer(invocation -> {
      ((Runnable)invocation.getArguments()[0]).run();
      return null;
    }).when(mock).run(Mockito.any(Runnable.class));
    Mockito.doAnswer(invocation -> {
      return ((Callable)invocation.getArguments()[0]).call();
    }).when(mock).call(Mockito.any(Callable.class));
    Mockito.doAnswer(invocation -> {
      return ((Callable)invocation.getArguments()[1]).call();
    }).when(mock).call(Mockito.any(UnitOfWork.class), Mockito.any(Callable.class));
    return mock;
  }

}
