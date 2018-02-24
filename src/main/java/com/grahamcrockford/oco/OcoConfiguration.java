package com.grahamcrockford.oco;

import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientConfiguration;

import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.grahamcrockford.oco.core.ExchangeConfiguration;
import com.grahamcrockford.oco.core.TelegramConfiguration;
import com.grahamcrockford.oco.db.DbConfiguration;

/**
 * Runtime config. Should really be broken up.
 */
public class OcoConfiguration extends Configuration {

  @NotNull
  @Min(1L)
  private int loopSeconds;

  @NotNull
  private String userName;

  @NotNull
  private String password;

  @NotNull
  private String secretKey;

  @NotNull
  private DbConfiguration database;

  @NotNull
  private boolean proxied;

  @Valid
  @NotNull
  private JerseyClientConfiguration jerseyClient = new JerseyClientConfiguration();

  private TelegramConfiguration telegram;
  private Map<String, ExchangeConfiguration> exchanges;


  public OcoConfiguration() {
    super();
  }

  @JsonProperty
  public int getLoopSeconds() {
    return loopSeconds;
  }

  @JsonProperty
  public void setLoopSeconds(int loopSeconds) {
    this.loopSeconds = loopSeconds;
  }

  @JsonProperty
  public String getUserName() {
    return userName;
  }

  @JsonProperty
  public void setUserName(String userName) {
    this.userName = userName;
  }

  @JsonProperty
  public String getPassword() {
    return password;
  }

  @JsonProperty
  public void setPassword(String password) {
    this.password = password;
  }

  @JsonProperty
  public String getSecretKey() {
    return secretKey;
  }

  @JsonProperty
  public void setSecretKey(String secretKey) {
    this.secretKey = secretKey;
  }

  @JsonProperty
  public DbConfiguration getDatabase() {
    return database;
  }

  @JsonProperty
  public void setDatabase(DbConfiguration database) {
    this.database = database;
  }

  @JsonProperty
  public TelegramConfiguration getTelegram() {
    return telegram;
  }

  @JsonProperty
  public void setTelegram(TelegramConfiguration telegram) {
    this.telegram = telegram;
  }

  @JsonProperty
  public Map<String, ExchangeConfiguration> getExchanges() {
    return exchanges;
  }

  @JsonProperty
  public void setExchanges(Map<String, ExchangeConfiguration> exchange) {
    this.exchanges = exchange;
  }

  @JsonProperty("jerseyClient")
  public JerseyClientConfiguration getJerseyClientConfiguration() {
      return jerseyClient;
  }

  @JsonProperty("jerseyClient")
  public void setJerseyClientConfiguration(JerseyClientConfiguration jerseyClient) {
      this.jerseyClient = jerseyClient;
  }

  @JsonProperty
  public boolean isProxied() {
    return proxied;
  }

  @JsonProperty
  public void setProxied(boolean proxied) {
    this.proxied = proxied;
  }
}