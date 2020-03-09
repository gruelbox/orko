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

import org.alfasoftware.morf.jdbc.h2.H2;
import org.alfasoftware.morf.jdbc.mysql.MySql;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.dialect.MySQL57Dialect;
import org.jooq.SQLDialect;

final class DialectResolver {

  private DialectResolver() {
    // Not instantiatable
  }

  static String hibernateDialect(String databaseType) {
    switch (databaseType) {
      case H2.IDENTIFIER:
        return H2Dialect.class.getName();
      case MySql.IDENTIFIER:
        return MySQL57Dialect.class.getName();
      default:
        throw new UnsupportedOperationException("Unknown dialect");
    }
  }

  static SQLDialect jooqDialect(String databaseType) {
    switch (databaseType) {
      case H2.IDENTIFIER:
        return SQLDialect.H2;
      case MySql.IDENTIFIER:
        return SQLDialect.MYSQL;
      default:
        throw new UnsupportedOperationException("Unknown dialect");
    }
  }
}
