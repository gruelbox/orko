package com.grahamcrockford.oco;

import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientConfiguration;

import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.grahamcrockford.oco.auth.AuthConfiguration;
import com.grahamcrockford.oco.db.DbConfiguration;
import com.grahamcrockford.oco.exchange.ExchangeConfiguration;
import com.grahamcrockford.oco.mq.MqConfiguration;
import com.grahamcrockford.oco.notification.TelegramConfiguration;

/**
 * Runtime config. Should really be broken up.
 */
public class OcoConfiguration extends Configuration {

  /**
   * Some operations require polling (exchanges with no websocket support,
   * cache timeouts etc).  This is the loop time.
   */
  @NotNull
  @Min(1L)
  private int loopSeconds;

  /**
   * Authentication configuration
   */
  @NotNull
  private AuthConfiguration auth;

  /**
   * Database configuration. If not provided, the application will use
   * volatile in-memory storage, which is obviously fine for trying things
   * out but quickly becomes useless in real life.
   */
  private DbConfiguration database;

  /**
   * Telegram configuration. Currently required for notifications.  Can
   * be left out but then you have no idea what the application is doing.
   */
  private TelegramConfiguration telegram;

  /**
   * MQ configuration. Required for communication when running separate
   * worker and web applications.
   */
  private MqConfiguration mq;

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

  @JsonProperty
  public MqConfiguration getMq() {
    return mq;
  }

  @JsonProperty
  public void setMq(MqConfiguration mq) {
    this.mq = mq;
  }
}