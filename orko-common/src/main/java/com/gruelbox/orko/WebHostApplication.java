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
package com.gruelbox.orko;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.servlet.ServletModule;
import com.gruelbox.orko.websocket.WebSocketBundleInit;
import com.gruelbox.tools.dropwizard.httpsredirect.HttpEnforcementConfiguration;
import com.gruelbox.tools.dropwizard.httpsredirect.HttpsEnforcementBundle;
import io.dropwizard.Configuration;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.websockets.WebsocketBundle;
import javax.inject.Inject;

public abstract class WebHostApplication<
        T extends Configuration & BaseApplicationConfiguration & HttpEnforcementConfiguration>
    extends BaseApplication<T> implements Module {

  @Inject private WebSocketBundleInit webSocketBundleInit;
  @Inject private UrlRewriteEnvironment urlRewriteEnvironment;

  private WebsocketBundle websocketBundle;

  @Override
  public void initialize(final Bootstrap<T> bootstrap) {
    bootstrap.addBundle(new AssetsBundle("/assets/", "/", "index.html"));
    super.initialize(bootstrap);
    websocketBundle = new WebsocketBundle(new Class[] {});
    bootstrap.addBundle(websocketBundle);
    bootstrap.addBundle(new HttpsEnforcementBundle());
  }

  @Override
  protected abstract Module createApplicationModule();

  @Override
  public final void run(final T configuration, final Environment environment) {
    urlRewriteEnvironment.init(environment);
    super.run(configuration, environment);
    webSocketBundleInit.init(websocketBundle);
  }

  @Override
  public void configure(Binder binder) {
    super.configure(binder);
    binder.install(new ServletModule());
  }
}
