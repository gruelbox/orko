package com.grahamcrockford.orko;

import javax.inject.Inject;
import javax.servlet.FilterRegistration;
import javax.ws.rs.client.Client;

import org.tuckey.web.filters.urlrewrite.UrlRewriteFilter;

import com.google.inject.Module;
import com.grahamcrockford.orko.OrkoConfiguration;
import com.grahamcrockford.orko.websocket.WebSocketBundleInit;
import com.palantir.websecurity.WebSecurityBundle;
import com.palantir.websecurity.WebSecurityConfiguration;

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
    bootstrap.addBundle(new WebSecurityBundle(WebSecurityConfiguration.builder()
        .contentSecurityPolicy("default-src 'self'; "
                             + "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; "
                             + "font-src 'self' https://fonts.googleapis.com; "
                             + "script-src 'self' 'unsafe-inline' https://s3.tradingview.com; "
                             + "frame-ancestors 'self';")
        .build()));
    super.initialize(bootstrap);
    websocketBundle = new WebsocketBundle(new Class[] {});
    bootstrap.addBundle(websocketBundle);
  }

  @Override
  protected abstract Module createApplicationModule(final OrkoConfiguration configuration, final Environment environment, Client jerseyClient);

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