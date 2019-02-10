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

import java.io.InputStream;
import java.net.URL;

import javax.servlet.FilterConfig;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.tuckey.web.filters.urlrewrite.Conf;
import org.tuckey.web.filters.urlrewrite.UrlRewriteFilter;

import com.gruelbox.tools.dropwizard.guice.EnvironmentInitialiser;

import io.dropwizard.setup.Environment;

class UrlRewriteEnvironment implements EnvironmentInitialiser {
  @Override
  public void init(Environment environment) {
    FilterRegistration.Dynamic urlRewriteFilter = environment.servlets()
        .addFilter("UrlRewriteFilter", new UrlRewriteFilterFixed());
    urlRewriteFilter.addMappingForUrlPatterns(null, true, "/*");
    urlRewriteFilter.setInitParameter("confPath", "urlrewrite.xml");
  }


  /**
   * TODO See https://github.com/paultuckey/urlrewritefilter/issues/224
   * Should be fixed by https://github.com/paultuckey/urlrewritefilter/pull/225
   * and can be removed if a 4.0.2+ version of UrlRewriteFilter is released.
   */
  private static final class UrlRewriteFilterFixed extends UrlRewriteFilter {

    @Override
    protected void loadUrlRewriter(FilterConfig filterConfig) throws ServletException {
      String confPath = filterConfig.getInitParameter("confPath");
      ServletContext context = filterConfig.getServletContext();
      try {
        final URL confUrl = getClass().getClassLoader().getResource(confPath);
        if (confUrl == null) {
          throw new IllegalArgumentException("Could not locate configuration at " + confPath);
        }
        final InputStream config = getClass().getClassLoader().getResourceAsStream(confPath);
        if (config == null) {
          throw new IllegalArgumentException("Could not get configuration stream at " + confPath);
        }
        Conf conf = new Conf(context, config, confPath, confUrl.toString(), false);
        checkConf(conf);
      } catch (Throwable e) {
        throw new ServletException(e);
      }
    }
  }
}