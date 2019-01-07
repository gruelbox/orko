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

package com.gruelbox.orko.support;

import javax.annotation.security.RolesAllowed;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.MoreObjects;
import com.gruelbox.orko.auth.Roles;
import com.gruelbox.tools.dropwizard.guice.resources.WebResource;

@Path("/support")
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class SupportResource implements WebResource {

  @GET
  @Path("/meta")
  @Timed
  @RolesAllowed(Roles.TRADER)
  public SupportMetadata getMeta() {
    return SupportMetadata.create(MoreObjects.firstNonNull(ReadVersion.readVersionInfoInManifest(), "Development build"));
  }
}