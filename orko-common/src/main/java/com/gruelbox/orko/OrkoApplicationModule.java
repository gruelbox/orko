package com.gruelbox.orko;

/*-
 * ===============================================================================L
 * Orko Common
 * ================================================================================
 * Copyright (C) 2018 - 2019 Graham Crockford
 * ================================================================================
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * ===============================================================================E
 */

import javax.ws.rs.client.Client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.servlet.ServletModule;
import com.gruelbox.orko.db.DbConfiguration;

import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.setup.Environment;

public class OrkoApplicationModule extends AbstractModule {

  @Override
  protected void configure() {
    install(new ServletModule());
    install(new CommonModule());
  }

  @Provides
  DbConfiguration dbConfiguration(OrkoConfiguration configuration) {
    return configuration.getDatabase();
  }

  @Provides
  ObjectMapper objectMapper(Environment environment) {
    return environment.getObjectMapper();
  }

  @Provides
  @Singleton
  Client jerseyClient(Environment environment, OrkoConfiguration configuration) {
    return new JerseyClientBuilder(environment)
      .using(configuration.getJerseyClientConfiguration())
      .build("client");
  }
}
