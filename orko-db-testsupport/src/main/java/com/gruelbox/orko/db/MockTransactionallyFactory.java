package com.gruelbox.orko.db;

/*-
 * ===============================================================================L
 * Orko DB Testsupport
 * ================================================================================
 * Copyright (C) 2018 - 2019 Graham Crockford
 * ================================================================================
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * ===============================================================================E
 */

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
