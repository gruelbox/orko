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
package com.gruelbox.orko.db;

/*-
 * ===============================================================================L
 * Orko Base
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

import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.AvailableSettings;

import com.google.common.base.Supplier;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.db.ManagedDataSource;
import io.dropwizard.db.PooledDataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.hibernate.SessionFactoryFactory;
import io.dropwizard.setup.Environment;
import io.dropwizard.util.Duration;
import jersey.repackaged.com.google.common.collect.ImmutableMap;

public class HibernateBundleFactory {

  public static <T extends Configuration> HibernateBundle<T> create(
      Function<T, DbConfiguration> dbConfiguration,
      Supplier<Set<EntityContribution>> additionalEntities) {

    // Defer collecting the entity list until after the creation of the injector
    // since otherwise the Application would have to know about all the entities
    // in use throughout the application.
    SessionFactoryFactory sessionFactoryFactory = new SessionFactoryFactory() {
      @Override
      public SessionFactory build(HibernateBundle<?> bundle,
          Environment environment,
          PooledDataSourceFactory dbConfig,
          ManagedDataSource dataSource,
          List<Class<?>> entities) {
        return super.build(
          bundle, environment, dbConfig, dataSource,
          FluentIterable.concat(entities, FluentIterable.from(additionalEntities.get()).transformAndConcat(EntityContribution::getEntities)).toList()
        );
      }
    };

    // Construct the bundle, without excessive knowledge of the application
    // configuration
    return new HibernateBundle<T>(ImmutableList.of(), sessionFactoryFactory) {
      @Override
      public DataSourceFactory getDataSourceFactory(T configuration) {
        DataSourceFactory dsf = new DataSourceFactory();
        dsf.setDriverClass(dbConfiguration.apply(configuration).getDriverClassName());
        dsf.setUrl(dbConfiguration.apply(configuration).getJdbcUrl());
        dsf.setProperties(ImmutableMap.of(
            "charset", "UTF-8",
            "hibernate.dialect", DialectResolver.hibernateDialect(dbConfiguration.apply(configuration).toConnectionResources().getDatabaseType()),
            AvailableSettings.LOG_SESSION_METRICS, "false"
        ));
        dsf.setMaxWaitForConnection(Duration.seconds(1));
        dsf.setValidationQuery("/* Health Check */ SELECT 1");
        dsf.setMinSize(1);
        dsf.setMaxSize(4); // 10 is the max on Heroku
        dsf.setCheckConnectionWhileIdle(false);
        return dsf;
      }
    };
  }
}
