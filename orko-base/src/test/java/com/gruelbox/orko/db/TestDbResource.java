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
package com.gruelbox.orko.db;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import io.dropwizard.testing.junit.ResourceTestRule;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.ws.rs.core.Response;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.jupiter.api.Tag;

@Tag("database")
public class TestDbResource {

  private static final String TEST_DATA = "TESTDATA";
  private static final DbDump DB_DUMP = mock(DbDump.class);

  @ClassRule
  public static final ResourceTestRule resources =
      ResourceTestRule.builder().addResource(new DbResource(DB_DUMP)).build();

  @Before
  public void setup() {
    reset(DB_DUMP);
  }

  @Test
  public void testGetDump() throws IOException {
    File tempFile = File.createTempFile("orko-db-dump-", ".zip");
    try {
      try (FileOutputStream fo = new FileOutputStream(tempFile)) {
        IOUtils.write(TEST_DATA, fo, UTF_8);
      }
      when(DB_DUMP.dump()).thenReturn(tempFile);
      try (Response response = resources.target("/db.zip").request("application/zip").get()) {
        ByteArrayInputStream stream = (ByteArrayInputStream) response.getEntity();
        String string = IOUtils.toString(stream, UTF_8);
        assertThat(string, equalTo(TEST_DATA));
      }
    } finally {
      tempFile.delete();
    }
  }
}
