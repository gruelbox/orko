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
import com.kjetland.dropwizard.activemq.ActiveMQConfig;
import com.kjetland.dropwizard.activemq.ActiveMQConfigHolder;

/**
 * Runtime config. Should really be broken up.
 */
public class OcoConfiguration extends Configuration implements ActiveMQConfigHolder {

  @NotNull
  @Min(1L)
  private int loopSeconds;

  @NotNull
  private String userName;

  @NotNull
  private String password;

  @Valid
  @NotNull
  private JerseyClientConfiguration jerseyClient = new JerseyClientConfiguration();

  @Valid
  @NotNull
  private ActiveMQConfig activeMQ;

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

  @Override
  @JsonProperty
  public ActiveMQConfig getActiveMQ() {
      return activeMQ;
  }
  @JsonProperty
  public void setActiveMQ(ActiveMQConfig activeMQ) {
    this.activeMQ = activeMQ;
    return;
  }
}