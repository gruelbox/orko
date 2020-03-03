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

import com.gruelbox.tools.dropwizard.guice.EnvironmentInitialiser;
import io.dropwizard.setup.Environment;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import javax.servlet.FilterConfig;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import org.apache.commons.io.IOUtils;
import org.tuckey.web.filters.urlrewrite.Conf;
import org.tuckey.web.filters.urlrewrite.UrlRewriteFilter;

class UrlRewriteEnvironment implements EnvironmentInitialiser {
  @Override
  public void init(Environment environment) {
    FilterRegistration.Dynamic urlRewriteFilter =
        environment.servlets().addFilter("UrlRewriteFilter", new UrlRewriteFilterFixed());
    urlRewriteFilter.addMappingForUrlPatterns(null, true, "/*");
    urlRewriteFilter.setInitParameter("confPath", "urlrewrite.xml");
  }

  /**
   * TODO See https://github.com/paultuckey/urlrewritefilter/issues/224 Should be fixed by
   * https://github.com/paultuckey/urlrewritefilter/pull/225 and can be removed if a 4.0.2+ version
   * of UrlRewriteFilter is released.
   */
  private static final class UrlRewriteFilterFixed extends UrlRewriteFilter {

    @Override
    protected void loadUrlRewriter(FilterConfig filterConfig) throws ServletException {
      String confPath = filterConfig.getInitParameter("confPath");
      ServletContext context = filterConfig.getServletContext();
      try {
        InputStream config =
            IOUtils.toInputStream(
                "<urlrewrite>\n"
                    + "    <rule>\n"
                    + "        <from>^/?(addCoin|scripts|job|coin).*$</from>\n"
                    + "        <to type=\"forward\">/index.html</to>\n"
                    + "    </rule>\n"
                    + "</urlrewrite>",
                StandardCharsets.UTF_8);
        Conf conf = new Conf(context, config, confPath, "HARDCODED", false);
        checkConf(conf);
      } catch (Exception e) {
        throw new ServletException(e);
      }
    }
  }
}
