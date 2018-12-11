package com.gruelbox.orko.db;

import org.alfasoftware.morf.jdbc.h2.H2;
import org.alfasoftware.morf.jdbc.mysql.MySql;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.dialect.MySQL57Dialect;
import org.jooq.SQLDialect;

class DialectResolver {

  static String hibernateDialect(String databaseType) {
    switch (databaseType) {
      case H2.IDENTIFIER: return H2Dialect.class.getName();
      case MySql.IDENTIFIER: return MySQL57Dialect.class.getName();
      default: throw new UnsupportedOperationException("Unknown dialect");
    }
  }

  static SQLDialect jooqDialect(String databaseType) {
    switch (databaseType) {
      case H2.IDENTIFIER: return SQLDialect.H2;
      case MySql.IDENTIFIER: return SQLDialect.MYSQL;
      default: throw new UnsupportedOperationException("Unknown dialect");
    }
  }
}
