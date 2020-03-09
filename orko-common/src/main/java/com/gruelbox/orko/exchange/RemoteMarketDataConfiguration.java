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
package com.gruelbox.orko.exchange;

import org.apache.commons.lang3.StringUtils;

public final class RemoteMarketDataConfiguration {

  private String webSocketUri;
  private String exchangeEndpointUri;

  public RemoteMarketDataConfiguration() {}

  public RemoteMarketDataConfiguration(String webSocketUri) {
    this.webSocketUri = webSocketUri;
  }

  public String getWebSocketUri() {
    return webSocketUri;
  }

  public void setWebSocketUri(String webSocketUri) {
    this.webSocketUri = webSocketUri;
  }

  public boolean isEnabled() {
    return StringUtils.isNotBlank(webSocketUri) && StringUtils.isNotBlank(exchangeEndpointUri);
  }

  public String getExchangeEndpointUri() {
    return exchangeEndpointUri;
  }

  public void setExchangeEndpointUri(String exchangeEndpointUri) {
    this.exchangeEndpointUri = exchangeEndpointUri;
  }
}
