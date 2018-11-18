package com.grahamcrockford.orko.db;

import javax.annotation.Nullable;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.alfasoftware.morf.jdbc.ConnectionResources;
import org.alfasoftware.morf.jdbc.ConnectionResourcesBean;
import org.alfasoftware.morf.jdbc.DatabaseType;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DbConfiguration {

  @NotNull
  @JsonProperty
  private String jdbcUrl = "jdbc:h2:file:orko.db";

  @Nullable
  @JsonProperty
  private String username;

  @Nullable
  @JsonProperty
  private String password;

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


  public String getJdbcUrl() {
    return jdbcUrl;
  }

  public void setJdbcUrl(String jdbcUrl) {
    this.jdbcUrl = jdbcUrl;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public int getLockSeconds() {
    return lockSeconds;
  }

  public void setLockSeconds(int lockSeconds) {
    this.lockSeconds = lockSeconds;
  }

  public ConnectionResources toConnectionResources() {
    ConnectionResourcesBean connectionResourcesBean = new ConnectionResourcesBean(DatabaseType.Registry.parseJdbcUrl(jdbcUrl));
    connectionResourcesBean.setUserName(username);
    connectionResourcesBean.setPassword(password);
    return connectionResourcesBean;
  }
}