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
package com.gruelbox.orko.websocket;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.gruelbox.orko.auth.AuthModule;
import javax.inject.Named;
import javax.inject.Singleton;

public class WebSocketModule extends AbstractModule {

  public static final String ENTRY_POINT = "/ws";

  @Override
  protected void configure() {
    // No-op
  }

  @Provides
  @Named(AuthModule.BIND_WEBSOCKET_ENTRY_POINT)
  @Singleton
  String webSocketEntryPoint() {
    return WebSocketModule.ENTRY_POINT;
  }
}
