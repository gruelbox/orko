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

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.knowm.xchange.service.account.AccountService;

import com.google.inject.Inject;
import com.gruelbox.orko.OrkoConfiguration;
import com.gruelbox.orko.exchange.PaperAccountService.Factory;

class AccountServiceFactoryImpl implements AccountServiceFactory {

  private final ExchangeService exchangeService;
  private final OrkoConfiguration configuration;
  private final Factory paperAccountServiceFactory;

  @Inject
  AccountServiceFactoryImpl(ExchangeService exchangeService, OrkoConfiguration configuration, PaperAccountService.Factory paperAccountServiceFactory) {
    this.exchangeService = exchangeService;
    this.configuration = configuration;
    this.paperAccountServiceFactory = paperAccountServiceFactory;
  }

  @Override
  public AccountService getForExchange(String exchange) {
    Map<String, ExchangeConfiguration> exchangeConfig = configuration.getExchanges();
    if (exchangeConfig == null) {
      return paperAccountServiceFactory.getForExchange(exchange);
    }
    final ExchangeConfiguration exchangeConfiguration = configuration.getExchanges().get(exchange);
    if (exchangeConfiguration == null || StringUtils.isEmpty(exchangeConfiguration.getApiKey())) {
      return paperAccountServiceFactory.getForExchange(exchange);
    }
    return exchangeService.get(exchange).getAccountService();
  }
}