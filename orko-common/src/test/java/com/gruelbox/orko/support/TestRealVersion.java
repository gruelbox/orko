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
package com.gruelbox.orko.support;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TestRealVersion {

  private static final String VALID_VERSION = "(\\d)+\\.(\\d)+\\.(\\d)+(-SNAPSHOT)?";

  @Test
  public void testVerifyCheckingRegex() {
    assertFalse("".matches(VALID_VERSION));
    assertFalse("1".matches(VALID_VERSION));
    assertFalse("0.1".matches(VALID_VERSION));
    assertFalse("0.1.".matches(VALID_VERSION));
    assertFalse("0.1.2-".matches(VALID_VERSION));
    assertFalse("0.1.2-SNAPSHOT-1".matches(VALID_VERSION));

    assertTrue("0.1.2".matches(VALID_VERSION));
    assertTrue("0.1.2-SNAPSHOT".matches(VALID_VERSION));
    assertTrue("0.14.21".matches(VALID_VERSION));
    assertTrue("1234.23.1234".matches(VALID_VERSION));
    assertTrue("1234.23.1234-SNAPSHOT".matches(VALID_VERSION));
  }

  @Test
  public void testGetVersion() {
    assertTrue(
        ReadVersion.readVersionInfoInManifest().equals("${project.version}")
            || ReadVersion.readVersionInfoInManifest().matches(VALID_VERSION));
  }
}
