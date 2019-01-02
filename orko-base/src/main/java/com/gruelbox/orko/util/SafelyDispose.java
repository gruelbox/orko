package com.gruelbox.orko.util;

/*-
 * ===============================================================================L
 * Orko Base
 * ================================================================================
 * Copyright (C) 2018 - 2019 Graham Crockford
 * ================================================================================
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * ===============================================================================E
 */

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reactivex.disposables.Disposable;

public class SafelyDispose {

  private static final Logger LOGGER = LoggerFactory.getLogger(SafelyDispose.class);

  public static void of(Disposable... disposables) {
    of(Arrays.asList(disposables));
  }

  public static void of(Iterable<Disposable> disposables) {
    disposables.forEach(d -> {
      if (d == null)
        return;
      try {
        d.dispose();
      } catch (Exception e) {
        LOGGER.error("Error disposing of subscription", e);
      }
    });
  }
}
