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

package com.gruelbox.orko;

import javax.inject.Inject;
import javax.servlet.FilterRegistration;

import org.tuckey.web.filters.urlrewrite.UrlRewriteFilter;

import com.google.inject.Module;
import com.gruelbox.orko.websocket.WebSocketBundleInit;
import com.gruelbox.tools.dropwizard.httpsredirect.HttpsEnforcementBundle;

import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.websockets.WebsocketBundle;

public abstract class WebHostApplication extends BaseApplication {

  @Inject private WebSocketBundleInit webSocketBundleInit;

  private WebsocketBundle websocketBundle;

  @Override
  public void initialize(final Bootstrap<OrkoConfiguration> bootstrap) {
    bootstrap.addBundle(new AssetsBundle("/assets/", "/", "index.html"));
    super.initialize(bootstrap);
    websocketBundle = new WebsocketBundle(new Class[] {});
    bootstrap.addBundle(websocketBundle);
    bootstrap.addBundle(new HttpsEnforcementBundle());
  }

  @Override
  protected abstract Module createApplicationModule();

  @Override
  public final void run(final OrkoConfiguration configuration, final Environment environment) {

    // Rewrite all UI URLs to index.html
    FilterRegistration.Dynamic urlRewriteFilter = environment.servlets()
        .addFilter("UrlRewriteFilter", new UrlRewriteFilter());
    urlRewriteFilter.addMappingForUrlPatterns(null, true, "/*");
    urlRewriteFilter.setInitParameter("confPath", "urlrewrite.xml");

    super.run(configuration, environment);

    webSocketBundleInit.init(websocketBundle);
  }
}