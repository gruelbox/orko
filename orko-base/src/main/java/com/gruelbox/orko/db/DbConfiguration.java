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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.util.Duration;
import java.sql.Driver;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import org.alfasoftware.morf.jdbc.ConnectionResources;
import org.alfasoftware.morf.jdbc.DatabaseType;
import org.hibernate.cfg.AvailableSettings;

public class DbConfiguration {

  @NotNull @JsonProperty
  private String connectionString =
      "h2:file:./orko.db;DB_CLOSE_DELAY=-1;DEFAULT_LOCK_TIMEOUT=60000";

  /**
   * How long database locks should persist for, in seconds. Too short and you'll end up workers
   * fighting over the same jobs. Too long and if instances go down, it'll take ages for other
   * instances to pick up their jobs.
   */
  @NotNull
  @Min(10L)
  @JsonProperty
  private int lockSeconds = 45;

  /**
   * A zip file containing a Morf database snapshot to load into the database on startup. Extract
   * from a running instance using {@link DbResource}.
   */
  @JsonProperty private String startPositionFile;

  public void setConnectionString(String connectionString) {
    this.connectionString = connectionString;
  }

  public int getLockSeconds() {
    return lockSeconds;
  }

  public void setLockSeconds(int lockSeconds) {
    this.lockSeconds = lockSeconds;
  }

  public ConnectionResources toConnectionResources() {
    return DatabaseType.Registry.urlToConnectionResources("jdbc:" + connectionString);
  }

  public String getDriverClassName() {
    return DatabaseType.Registry.findByIdentifier(toConnectionResources().getDatabaseType())
        .driverClassName();
  }

  @SuppressWarnings("unchecked")
  public Class<? extends Driver> getDriver() {
    try {
      return (Class<? extends Driver>) Class.forName(getDriverClassName());
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  public String getJdbcUrl() {
    return "jdbc:" + connectionString;
  }

  public String getStartPositionFile() {
    return startPositionFile;
  }

  public DataSourceFactory toDataSourceFactory() {
    DataSourceFactory dsf = new DataSourceFactory();
    dsf.setDriverClass(getDriverClassName());
    dsf.setUrl(getJdbcUrl());
    dsf.setProperties(
        ImmutableMap.of(
            "charset",
            "UTF-8",
            "hibernate.dialect",
            com.gruelbox.orko.db.DialectResolver.hibernateDialect(
                toConnectionResources().getDatabaseType()),
            AvailableSettings.LOG_SESSION_METRICS,
            "false"));
    dsf.setMaxWaitForConnection(Duration.seconds(1));
    dsf.setValidationQuery("/* Health Check */ SELECT 1");
    dsf.setMinSize(1);
    dsf.setMaxSize(4); // 10 is the max on Heroku
    dsf.setCheckConnectionWhileIdle(false);
    return dsf;
  }
}
