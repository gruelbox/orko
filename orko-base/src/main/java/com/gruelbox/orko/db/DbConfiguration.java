package com.gruelbox.orko.db;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.alfasoftware.morf.jdbc.ConnectionResources;
import org.alfasoftware.morf.jdbc.DatabaseType;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DbConfiguration {

  @NotNull
  @JsonProperty
  private String connectionString = "h2:file:./orko.db;DB_CLOSE_DELAY=-1;MVCC=TRUE;DEFAULT_LOCK_TIMEOUT=60000";

  /**
   * How long database locks should persist for, in seconds.  Too short and
   * you'll end up workers fighting over the same jobs.  Too long
   * and if instances go down, it'll take ages for other instances to pick
   * up their jobs.
   */
  @NotNull
  @Min(10L)
  @JsonProperty
  private int lockSeconds = 10;

  public String getConnectionString() {
    return connectionString;
  }

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
}