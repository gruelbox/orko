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
package com.gruelbox.orko.docker;

import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import org.apache.commons.text.StrLookup;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class TestYamlEscapingStrLookupAdapter {

  @Mock private StrLookup<Object> delegate;

  @Test
  public void testPassThrough() {
    MockitoAnnotations.initMocks(this);
    YamlEscapingStrLookupAdapter<Object> onTest = new YamlEscapingStrLookupAdapter<>(delegate);
    String input = "${variable}";
    assertThat(onTest.lookup(input), nullValue());
  }

  @Test
  public void testNothingToEscape() {
    MockitoAnnotations.initMocks(this);
    YamlEscapingStrLookupAdapter<Object> onTest = new YamlEscapingStrLookupAdapter<>(delegate);
    String input = "${variable}";
    Mockito.when(delegate.lookup(input)).thenReturn("value");
    assertThat(onTest.lookup(input), Matchers.equalTo("value"));
  }

  @Test
  public void testEscape() {
    MockitoAnnotations.initMocks(this);
    YamlEscapingStrLookupAdapter<Object> onTest = new YamlEscapingStrLookupAdapter<>(delegate);
    String input = "${variable}";
    Mockito.when(delegate.lookup(input)).thenReturn("'value'");
    assertThat(onTest.lookup(input), Matchers.equalTo("''value''"));
  }
}
