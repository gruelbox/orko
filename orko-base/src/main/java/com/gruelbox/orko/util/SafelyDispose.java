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
package com.gruelbox.orko.util;

import io.reactivex.disposables.Disposable;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SafelyDispose {

  private static final Logger LOGGER = LoggerFactory.getLogger(SafelyDispose.class);

  private SafelyDispose() {
    // Not instantiatable
  }

  public static void of(Disposable... disposables) {
    of(Arrays.asList(disposables));
  }

  public static void of(Iterable<Disposable> disposables) {
    disposables.forEach(
        d -> {
          if (d == null) return;
          Safely.run("disposing of subscription", d::dispose);
        });
  }
}
