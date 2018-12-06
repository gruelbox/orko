package com.gruelbox.orko.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.function.Supplier;

import org.alfasoftware.morf.jdbc.ConnectionResources;
import org.alfasoftware.morf.jdbc.h2.H2;
import org.alfasoftware.morf.jdbc.mysql.MySql;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class ConnectionSource {

  private final Provider<DbConfiguration> dbConfiguration;
  private final ThreadLocal<Connection> currentConnection = ThreadLocal.withInitial(() -> null);
  private final Provider<ConnectionResources>  connectionResources;

  @Inject
  ConnectionSource(Provider<DbConfiguration> dbConfiguration, Provider<ConnectionResources> connectionResources) {
    this.dbConfiguration = dbConfiguration;
    this.connectionResources = connectionResources;
  }

  Connection newStandaloneConnection() throws SQLException {
    return DriverManager.getConnection("jdbc:" + dbConfiguration.get().getConnectionString());
  }

  Connection currentConnection() {
    if (!isConnectionOpen())
      throw new TransactionNotStartedException();
    return currentConnection.get();
  }

  public void withCurrentConnection(Runnable runnable) {
    withNewConnection(dsl -> runnable.run());
  }

  public <T> T getWithNewConnection(Supplier<T> supplier) {
    return getWithNewConnection(dsl ->  supplier.get());
  }

  public void withNewConnection(Work work) {
    getWithNewConnection(dsl -> {
      work.work(dsl);
      return null;
    });
  }

  public <T> T getWithNewConnection(ReturningWork<T> supplier) {
    if (isConnectionOpen())
      throw new TransactionAlreadyStartedException();
    boolean success = false;
    try {
      currentConnection.set(newStandaloneConnection());
      try {
        currentConnection.get().setAutoCommit(true);
        T result = getWithCurrentConnection(supplier);
        success = true;
        return result;
      } finally {
        Connection connection = currentConnection.get();
        currentConnection.remove();
        try {
          if (success) {
            connection.commit();
          }
        } finally {
          connection.close();
        }
      }
    } catch (SQLException e) {
      throw new RuntimeSqlException(e);
    }
  }

  public void withCurrentConnection(Work work) {
    getWithCurrentConnection(dsl -> {
      work.work(dsl);
      return null;
    });
  }

  public <T> T getWithCurrentConnection(ReturningWork<T> supplier) {
    try {
      return supplier.work(DSL.using(currentConnection(), jooqDialect()));
    } catch (SQLException e) {
      throw new RuntimeSqlException(e);
    }
  }

  private SQLDialect jooqDialect() {
    switch (connectionResources.get().getDatabaseType()) {
      case H2.IDENTIFIER: return SQLDialect.H2;
      case MySql.IDENTIFIER: return SQLDialect.MYSQL;
      default: throw new UnsupportedOperationException("Unknown dialect");
    }
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

  public static final class TransactionNotStartedException extends RuntimeException {
    private static final long serialVersionUID = 615154990760776016L;
  }

  public static final class TransactionAlreadyStartedException extends RuntimeException {
    private static final long serialVersionUID = 8666891802474259008L;
  }
}