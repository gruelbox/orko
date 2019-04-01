package com.gruelbox.orko.util;

import java.math.BigDecimal;

public class MoreBigDecimals {

  /**
   * Removes any superfluous significant figures in a {@link BigDecimal},
   * reducing the scale to remove any trailing zeros without forcing
   * scientific notation.
   *
   * @param bigDecimal The value.
   * @return The stripped result.
   */
  public static BigDecimal stripZeros(BigDecimal bigDecimal) {
    BigDecimal result = bigDecimal.stripTrailingZeros();
    if (result.scale() < 0)
      result = result.setScale(0);
    return result;
  }

}
