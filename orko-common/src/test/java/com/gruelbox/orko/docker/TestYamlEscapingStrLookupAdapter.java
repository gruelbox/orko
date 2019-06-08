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

  @Mock
  private StrLookup<Object> delegate;

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
