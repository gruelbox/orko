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
package com.gruelbox.orko.app.marketdata;

import com.google.inject.Module;
import com.gruelbox.orko.BaseApplication;
import com.gruelbox.orko.websocket.WebSocketBundleInit;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.websockets.WebsocketBundle;
import javax.inject.Inject;

public class MarketDataApplication extends BaseApplication<MarketDataAppConfiguration> {

  @Inject private WebSocketBundleInit webSocketBundleInit;
  private WebsocketBundle websocketBundle;

  public static void main(final String... args) throws Exception {
    new MarketDataApplication().run(args);
  }

  @Override
  public String getName() {
    return "Orko market data application";
  }

  @Override
  public void initialize(Bootstrap<MarketDataAppConfiguration> bootstrap) {
    super.initialize(bootstrap);
    websocketBundle = new WebsocketBundle(new Class[] {});
    bootstrap.addBundle(websocketBundle);
  }

  @Override
  protected Module createApplicationModule() {
    return new MarketDataAppModule();
  }

  @Override
  public final void run(
      final MarketDataAppConfiguration configuration, final Environment environment) {
    super.run(configuration, environment);
    webSocketBundleInit.init(websocketBundle);
  }
}
