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
package com.gruelbox.orko.app.monolith;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.google.inject.util.Providers;
import com.gruelbox.orko.BaseApplicationConfiguration;
import com.gruelbox.orko.auth.AuthConfiguration;
import com.gruelbox.orko.db.DbConfiguration;
import com.gruelbox.orko.exchange.ExchangeConfiguration;
import com.gruelbox.orko.exchange.RemoteMarketDataConfiguration;
import com.gruelbox.orko.job.script.ScriptConfiguration;
import com.gruelbox.orko.jobrun.spi.JobRunConfiguration;
import com.gruelbox.orko.notification.AppriseConfiguration;
import com.gruelbox.orko.notification.TelegramConfiguration;
import com.gruelbox.orko.wiring.BackgroundProcessingConfiguration;
import com.gruelbox.tools.dropwizard.httpsredirect.HttpEnforcementConfiguration;
import com.gruelbox.tools.dropwizard.httpsredirect.HttpsResponsibility;
import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.server.AbstractServerFactory;
import java.util.HashMap;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.Min;

/** Configuration for the monolith application. */
public class MonolithConfiguration extends Configuration
    implements HttpEnforcementConfiguration,
        BackgroundProcessingConfiguration,
        ScriptConfiguration,
        BaseApplicationConfiguration {

  /**
   * Some operations require polling (exchanges with no websocket support, cache timeouts etc). This
   * is the loop time.
   */
  @Min(1L)
  @JsonProperty
  private int loopSeconds = 15;

  /** Authentication configuration */
  @Valid @JsonProperty private AuthConfiguration auth;

  /**
   * Database configuration. If not provided, the application will use volatile in-memory storage,
   * which is obviously fine for trying things out but quickly becomes useless in real life.
   */
  @Valid @JsonProperty private DbConfiguration database = new DbConfiguration();

  /**
   * Telegram configuration. Currently required for notifications. Can be left out but then you have
   * no idea what the application is doing.
   */
  @Valid @JsonProperty private TelegramConfiguration telegram;

  @Valid @JsonProperty private AppriseConfiguration apprise;

  @JsonProperty private String scriptSigningKey;

  @Valid
  @JsonProperty("jerseyClient")
  private JerseyClientConfiguration jerseyClient;

  @Valid @JsonProperty private Map<String, ExchangeConfiguration> exchanges = new HashMap<>();

  @Valid @JsonProperty
  private RemoteMarketDataConfiguration remoteMarketData = new RemoteMarketDataConfiguration();

  private boolean childProcess;

  public MonolithConfiguration() {
    super();
  }

  @Override
  public int getLoopSeconds() {
    return loopSeconds;
  }

  public void setLoopSeconds(int loopSeconds) {
    this.loopSeconds = loopSeconds;
  }

  public AuthConfiguration getAuth() {
    return auth;
  }

  public void setAuth(AuthConfiguration auth) {
    this.auth = auth;
  }

  public DbConfiguration getDatabase() {
    return database;
  }

  public void setDatabase(DbConfiguration database) {
    this.database = database;
  }

  public TelegramConfiguration getTelegram() {
    return telegram;
  }

  public void setTelegram(TelegramConfiguration telegram) {
    this.telegram = telegram;
  }

  public AppriseConfiguration getApprise() {
    return apprise;
  }

  public void setApprise(AppriseConfiguration apprise) {
    this.apprise = apprise;
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

  public RemoteMarketDataConfiguration getRemoteMarketData() {
    return remoteMarketData;
  }

  public void setRemoteMarketData(RemoteMarketDataConfiguration remoteMarketData) {
    this.remoteMarketData = remoteMarketData;
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

  @Override
  public boolean isHttpsOnly() {
    if (auth == null) {
      return false;
    }
    return auth.isHttpsOnly();
  }

  @Override
  public HttpsResponsibility getHttpResponsibility() {
    if (auth == null) {
      return HttpsResponsibility.HTTPS_DIRECT;
    }
    return auth.isProxied() ? HttpsResponsibility.HTTPS_AT_PROXY : HttpsResponsibility.HTTPS_DIRECT;
  }

  /**
   * Takes all the configuration components and binds them to the injector so they become available
   * to modules throughout the application.
   *
   * @param binder The Guice binder.
   */
  public void bind(Binder binder) {
    binder.bind(BackgroundProcessingConfiguration.class).toInstance(this);
    binder.bind(ScriptConfiguration.class).toInstance(this);
    binder.bind(DbConfiguration.class).toProvider(Providers.of(database));
    binder.bind(AuthConfiguration.class).toProvider(Providers.of(auth));
    binder.bind(JerseyClientConfiguration.class).toProvider(Providers.of(jerseyClient));
    binder.bind(TelegramConfiguration.class).toProvider(Providers.of(telegram));
    binder.bind(AppriseConfiguration.class).toProvider(Providers.of(apprise));
    binder
        .bind(new TypeLiteral<Map<String, ExchangeConfiguration>>() {})
        .toProvider(Providers.of(exchanges));
    binder.bind(RemoteMarketDataConfiguration.class).toInstance(this.remoteMarketData);

    JobRunConfiguration jobRunConfiguration = new JobRunConfiguration();
    jobRunConfiguration.setDatabaseLockSeconds(database.getLockSeconds());
    jobRunConfiguration.setGuardianLoopSeconds(loopSeconds);
    binder.bind(JobRunConfiguration.class).toInstance(jobRunConfiguration);
  }
}
