/**
 * Orko
 * Copyright © 2018-2019 Graham Crockford
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.gruelbox.orko.exchange;

import org.knowm.xchange.service.trade.TradeService;

import com.google.inject.Inject;
import com.gruelbox.orko.OrkoConfiguration;

class TradeServiceFactoryImpl extends AbstractExchangeServiceFactory<TradeService>
                              implements TradeServiceFactory {

  private final ExchangeService exchangeService;
  private final PaperTradeService.Factory paperTradeServiceFactory;

  @Inject
  TradeServiceFactoryImpl(ExchangeService exchangeService,
                          OrkoConfiguration configuration,
                          PaperTradeService.Factory paperTradeServiceFactory) {
    super(configuration);
    this.exchangeService = exchangeService;
    this.paperTradeServiceFactory = paperTradeServiceFactory;
  }

  @Override
  protected ExchangeServiceFactory<TradeService> getRealFactory() {
    return exchange -> exchangeService.get(exchange).getTradeService();
  }

  @Override
  protected ExchangeServiceFactory<TradeService> getPaperFactory() {
    return paperTradeServiceFactory;
  }
}