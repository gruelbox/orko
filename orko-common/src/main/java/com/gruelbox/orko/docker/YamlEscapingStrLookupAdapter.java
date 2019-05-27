package com.gruelbox.orko.docker;

import org.apache.commons.text.StrLookup;

/**
 * Adapter for {@link StrLookup}s which escapes the result for use
 * in YAML. Note that it's assumed the result will be replacing
 * a value which is already expressed inside SINGLE quotes.
 */
public class YamlEscapingStrLookupAdapter<T> extends StrLookup<T> {

  private final StrLookup<T> delegate;

  public YamlEscapingStrLookupAdapter(StrLookup<T> delegate) {
    this.delegate = delegate;
  }

  @Override
  public String lookup(String key) {
    String result = delegate.lookup(key);
    if (result == null)
      return null;
    return result.replace("'", "''");
  }
}