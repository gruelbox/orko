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


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.alfasoftware.morf.jdbc.ConnectionResources;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.annotation.Timed;
import com.gruelbox.tools.dropwizard.guice.resources.WebResource;

/**
 * Diagnostic endpoint. Produces a dump of the database for import into a test
 * instance.
 *
 * @author Graham Crockford
 */
@Path("/db.zip")
@Singleton
public class DbResource implements WebResource {

  private static final Logger LOGGER = LoggerFactory.getLogger(DbResource.class);

  private final ConnectionResources connectionResources;

  @Inject
  DbResource(ConnectionResources connectionResources) {
    this.connectionResources = connectionResources;
  }

  @GET
  @Timed
  @Produces("application/zip")
  public Response check() {
    return Response.ok(new StreamingOutput() {
      @Override
      public void write(OutputStream output) throws IOException, WebApplicationException {
        File tempFile = DbDump.dump(connectionResources);
        try {
          try (BufferedInputStream input = new BufferedInputStream(new FileInputStream(tempFile))) {
            IOUtils.copy(input, output);
          }
        } finally {
          if (!tempFile.delete())
            LOGGER.warn("Failed to delete tempfile: {}", tempFile.getName());
        }
      }
    }).build();
  }
}
