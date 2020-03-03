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

import com.fasterxml.jackson.databind.JsonMappingException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation if {@link ExceptionMapper} to send down a "400 Bad Request" response in the event
 * that unmappable JSON is received.
 *
 * <p>Note that {@link javax.ws.rs.ext.Provider} annotation was include up to Jackson 2.7, but
 * removed from 2.8 (as per [jaxrs-providers#22]
 *
 * @since 2.2
 */
public class JerseyMappingErrorLoggingExceptionHandler
    implements ExceptionMapper<JsonMappingException> {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(JerseyMappingErrorLoggingExceptionHandler.class);

  @Override
  public Response toResponse(JsonMappingException exception) {
    LOGGER.error("JSON mapping error at " + exception.getPath(), exception);
    return Response.status(Response.Status.BAD_REQUEST)
        .entity(exception.getMessage())
        .type("text/plain")
        .build();
  }
}
