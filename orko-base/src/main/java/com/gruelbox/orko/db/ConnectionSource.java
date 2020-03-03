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

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import java.sql.Connection;
import java.sql.SQLException;
import org.alfasoftware.morf.jdbc.ConnectionResources;
import org.hibernate.SessionFactory;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

@Singleton
public class ConnectionSource {

  private final ThreadLocal<Connection> currentConnection = ThreadLocal.withInitial(() -> null);
  private final Provider<ConnectionResources> connectionResources;
  private final Provider<SessionFactory> sessionFactory;

  @Inject
  ConnectionSource(
      Provider<SessionFactory> sessionFactory, Provider<ConnectionResources> connectionResources) {
    this.sessionFactory = sessionFactory;
    this.connectionResources = connectionResources;
  }

  public void withCurrentConnection(Runnable runnable) {
    withCurrentConnection(dsl -> runnable.run());
  }

  public void withCurrentConnection(Work work) {
    getWithCurrentConnection(
        dsl -> {
          work.work(dsl);
          return null;
        });
  }

  public <T> T getWithCurrentConnection(ReturningWork<T> supplier) {
    return sessionFactory
        .get()
        .getCurrentSession()
        .doReturningWork(
            connection -> {
              DSLContext dsl =
                  DSL.using(
                      connection,
                      DialectResolver.jooqDialect(connectionResources.get().getDatabaseType()));
              return supplier.work(dsl);
            });
  }

  public interface ReturningWork<T> {
    T work(DSLContext dsl) throws SQLException;
  }

  public interface Work {
    void work(DSLContext dsl) throws SQLException;
  }

  public boolean isConnectionOpen() {
    return currentConnection.get() != null;
  }

  public static final class RuntimeSqlException extends RuntimeException {
    private static final long serialVersionUID = -1156191316885665707L;

    public RuntimeSqlException(SQLException cause) {
      super(cause);
    }

    public RuntimeSqlException(String message, SQLException cause) {
      super(message, cause);
    }
  }
}
