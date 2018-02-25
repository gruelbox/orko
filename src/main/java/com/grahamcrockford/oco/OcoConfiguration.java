package com.grahamcrockford.oco;

import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientConfiguration;

import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.grahamcrockford.oco.auth.AuthConfiguration;
import com.grahamcrockford.oco.core.ExchangeConfiguration;
import com.grahamcrockford.oco.db.DbConfiguration;
import com.grahamcrockford.oco.telegram.TelegramConfiguration;

/**
 * Runtime config. Should really be broken up.
 */
public class OcoConfiguration extends Configuration {

  @NotNull
  @Min(1L)
  private int loopSeconds;

  @NotNull
  private AuthConfiguration auth;

  @NotNull
  private DbConfiguration database;

  @NotNull
  private TelegramConfiguration telegram;

  @Valid
  @NotNull
  private JerseyClientConfiguration jerseyClient = new JerseyClientConfiguration();

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
  public AuthConfiguration getAuth() {
    return auth;
  }

  @JsonProperty
  public void setAuth(AuthConfiguration auth) {
    this.auth = auth;
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
}