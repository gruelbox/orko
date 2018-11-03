package com.grahamcrockford.orko.allinone;

import java.util.Set;

import javax.inject.Inject;
import javax.servlet.FilterRegistration;
import javax.ws.rs.client.Client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tuckey.web.filters.urlrewrite.UrlRewriteFilter;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceFilter;
import com.grahamcrockford.orko.OrkoConfiguration;
import com.grahamcrockford.orko.websocket.WebSocketBundleInit;
import com.grahamcrockford.orko.wiring.EnvironmentInitialiser;

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.websockets.WebsocketBundle;

public class AllInOneApplication extends Application<OrkoConfiguration> {

  private static final Logger LOGGER = LoggerFactory.getLogger(AllInOneApplication.class);

  public static void main(final String[] args) throws Exception {
    new AllInOneApplication().run(args);
  }

  @Inject private Set<EnvironmentInitialiser> environmentInitialisers;
  @Inject private WebSocketBundleInit webSocketBundleInit;

  private WebsocketBundle websocketBundle;


  @Override
  public String getName() {
    return "Orko all-in-one application";
  }

  @Override
  public void initialize(final Bootstrap<OrkoConfiguration> bootstrap) {
    bootstrap.addBundle(new AssetsBundle("/assets/", "/", "index.html"));
    bootstrap.setConfigurationSourceProvider(
      new SubstitutingSourceProvider(
        bootstrap.getConfigurationSourceProvider(),
        new EnvironmentVariableSubstitutor(false)
      )
    );
    websocketBundle = new WebsocketBundle(new Class[] {});
    bootstrap.addBundle(websocketBundle);
  }

  @Override
  public void run(final OrkoConfiguration configuration, final Environment environment) {

    // Rewrite all UI URLs to index.html
    FilterRegistration.Dynamic urlRewriteFilter = environment.servlets()
        .addFilter("UrlRewriteFilter", new UrlRewriteFilter());
    urlRewriteFilter.addMappingForUrlPatterns(null, true, "/*");
    urlRewriteFilter.setInitParameter("confPath", "urlrewrite.xml");

    // Jersey client
    final Client jerseyClient = new JerseyClientBuilder(environment).using(configuration.getJerseyClientConfiguration()).build(getName());

    // Injector
    final Injector injector = Guice.createInjector(new AllInOneModule(configuration, environment.getObjectMapper(), jerseyClient, environment));
    injector.injectMembers(this);

    GuiceFilter guiceFilter = injector.getInstance(GuiceFilter.class);
    environment.servlets().addFilter("GuiceFilter", guiceFilter).addMappingForUrlPatterns(null, false, "/*");
    environment.admin().addFilter("GuiceFilter", guiceFilter).addMappingForUrlPatterns(null, false, "/*");

    // Any environment initialisation
    environmentInitialisers.stream()
      .peek(t -> LOGGER.info("Initialising environment for {}", t))
      .forEach(t -> t.init(environment));

    webSocketBundleInit.init(websocketBundle);
  }
}