package com.grahamcrockford.orko;

import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.grahamcrockford.orko.auth.AuthConfiguration;
import com.grahamcrockford.orko.db.DbConfiguration;
import com.grahamcrockford.orko.exchange.ExchangeConfiguration;
import com.grahamcrockford.orko.mq.MqConfiguration;
import com.grahamcrockford.orko.notification.TelegramConfiguration;
import com.palantir.websecurity.WebSecurityConfigurable;
import com.palantir.websecurity.WebSecurityConfiguration;

import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.server.AbstractServerFactory;

/**
 * Runtime config. Should really be broken up.
 */
public class OrkoConfiguration extends Configuration implements WebSecurityConfigurable {

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

  public OrkoConfiguration() {
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

  public String getRootPath() {
    AbstractServerFactory serverFactory = (AbstractServerFactory) getServerFactory();
    return serverFactory.getJerseyRootPath().orElse("/") + "*";
  }

  @JsonProperty("webSecurity")
  @NotNull
  @Valid
  private final WebSecurityConfiguration webSecurity = WebSecurityConfiguration.DEFAULT;

  @Override
  public WebSecurityConfiguration getWebSecurityConfiguration() {
      return this.webSecurity;
  }
}