/**
 * Orko
 * Copyright © 2018-2019 Graham Crockford
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.gruelbox.orko.app.marketdata;

import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.Min;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.google.inject.util.Providers;
import com.gruelbox.orko.BaseApplicationConfiguration;
import com.gruelbox.orko.exchange.ExchangeConfiguration;
import com.gruelbox.orko.job.script.ScriptConfiguration;
import com.gruelbox.orko.notification.TelegramConfiguration;
import com.gruelbox.orko.wiring.BackgroundProcessingConfiguration;

import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.server.AbstractServerFactory;

/**
 * Configuration for the market data application.
 */
public class MarketDataAppConfiguration extends Configuration implements BackgroundProcessingConfiguration, ScriptConfiguration, BaseApplicationConfiguration {

  /**
   * Some operations require polling (exchanges with no websocket support,
   * cache timeouts etc).  This is the loop time.
   */
  @Min(1L)
  @JsonProperty
  private int loopSeconds = 15;

  /**
   * Telegram configuration. Currently required for notifications.  Can
   * be left out but then you have no idea what the application is doing.
   */
  @Valid
  @JsonProperty
  private TelegramConfiguration telegram;

  @JsonProperty
  private String scriptSigningKey;

  @Valid
  @JsonProperty("jerseyClient")
  private JerseyClientConfiguration jerseyClient;

  @JsonProperty
  private Map<String, ExchangeConfiguration> exchanges;

  private boolean childProcess;

  public MarketDataAppConfiguration() {
    super();
  }

  @Override
  public int getLoopSeconds() {
    return loopSeconds;
  }

  public void setLoopSeconds(int loopSeconds) {
    this.loopSeconds = loopSeconds;
  }

  public TelegramConfiguration getTelegram() {
    return telegram;
  }

  public void setTelegram(TelegramConfiguration telegram) {
    this.telegram = telegram;
  }

  public Map<String, ExchangeConfiguration> getExchanges() {
    return exchanges;
  }

  public void setExchanges(Map<String, ExchangeConfiguration> exchange) {
    exchanges = exchange;
  }

  @Override
  public String getScriptSigningKey() {
    return scriptSigningKey;
  }

  public void setScriptSigningKey(String scriptSigningKey) {
    this.scriptSigningKey = scriptSigningKey;
  }

  public JerseyClientConfiguration getJerseyClientConfiguration() {
    return jerseyClient;
  }

  public void setJerseyClientConfiguration(JerseyClientConfiguration jerseyClient) {
    this.jerseyClient = jerseyClient;
  }

  public void setChildProcess(boolean childProcess) {
    this.childProcess = childProcess;
  }

  @Override
  public boolean isChildProcess() {
    return childProcess;
  }

  public String getRootPath() {
    AbstractServerFactory serverFactory = (AbstractServerFactory) getServerFactory();
    return serverFactory.getJerseyRootPath().orElse("/") + "*";
  }

  /**
   * Takes all the configuration components and binds them to the injector
   * so they become available to modules throughout the application.
   *
   * @param binder The Guice binder.
   */
  public void bind(Binder binder) {
    binder.bind(BackgroundProcessingConfiguration.class).toInstance(this);
    binder.bind(ScriptConfiguration.class).toInstance(this);
    binder.bind(new TypeLiteral<Map<String, ExchangeConfiguration>>() {}).toProvider(Providers.of(exchanges));
  }
}
