package com.gruelbox.orko.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;

import org.alfasoftware.morf.jdbc.ConnectionResources;
import org.hibernate.SessionFactory;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
@Deprecated
public class ConnectionSource {

  private final ThreadLocal<Connection> currentConnection = ThreadLocal.withInitial(() -> null);
  private final Provider<ConnectionResources> connectionResources;
  private final Provider<SessionFactory> sessionFactory;
  private final Transactionally transactionally;

  @Inject
  ConnectionSource(Provider<SessionFactory> sessionFactory,
                   Provider<ConnectionResources> connectionResources,
                   Transactionally transactionally) {
    this.sessionFactory = sessionFactory;
    this.connectionResources = connectionResources;
    this.transactionally = transactionally;
  }

  public void withCurrentConnection(Runnable runnable) {
    withCurrentConnection(dsl -> runnable.run());
  }

  public <T> T getWithNewConnection(Supplier<T> supplier) {
    return getWithCurrentConnection(dsl ->  supplier.get());
  }

  public void withNewConnection(Work work) {
    withCurrentConnection(work);
  }

  public <T> T getWithNewConnection(ReturningWork<T> supplier) {
    return getWithCurrentConnection(supplier);
  }

  public void withCurrentConnection(Work work) {
    getWithCurrentConnection(dsl -> {
      work.work(dsl);
      return null;
    });
  }

  public <T> T getWithCurrentConnection(ReturningWork<T> supplier) {
    return transactionally.call(() -> {
      return sessionFactory.get()
          .getCurrentSession()
          .doReturningWork(connection -> supplier.work(DSL.using(connection, DialectResolver.jooqDialect(connectionResources.get().getDatabaseType()))));
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