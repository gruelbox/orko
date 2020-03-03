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

import com.google.inject.Inject;
import java.util.Map;
import org.knowm.xchange.service.trade.TradeService;

class TradeServiceFactoryImpl extends AbstractExchangeServiceFactory<TradeService>
    implements TradeServiceFactory {

  private final ExchangeService exchangeService;
  private final PaperTradeService.Factory paperTradeServiceFactory;
  private final RemoteTradeService.Factory remoteTradeServiceFactory;
  private final RemoteMarketDataConfiguration remoteConfiguration;

  @Inject
  TradeServiceFactoryImpl(
      ExchangeService exchangeService,
      Map<String, ExchangeConfiguration> configuration,
      PaperTradeService.Factory paperTradeServiceFactory,
      RemoteTradeService.Factory remoteTradeServiceFactory,
      RemoteMarketDataConfiguration remoteConfiguration) {
    super(configuration);
    this.exchangeService = exchangeService;
    this.paperTradeServiceFactory = paperTradeServiceFactory;
    this.remoteTradeServiceFactory = remoteTradeServiceFactory;
    this.remoteConfiguration = remoteConfiguration;
  }

  @Override
  protected ExchangeServiceFactory<TradeService> getRealFactory() {
    if (remoteConfiguration.isEnabled()) {
      return exchange -> remoteTradeServiceFactory.create(exchange);
    } else {
      return exchange -> exchangeService.get(exchange).getTradeService();
    }
  }

  @Override
  protected ExchangeServiceFactory<TradeService> getPaperFactory() {
    if (remoteConfiguration.isEnabled()) {
      return exchange -> remoteTradeServiceFactory.create(exchange);
    } else {
      return paperTradeServiceFactory;
    }
  }
}
