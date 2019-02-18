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

import org.knowm.xchange.service.account.AccountService;

import com.google.inject.Inject;
import com.gruelbox.orko.OrkoConfiguration;
import com.gruelbox.orko.exchange.PaperAccountService.Factory;

class AccountServiceFactoryImpl extends AbstractExchangeServiceFactory<AccountService>
                                implements AccountServiceFactory {

  private final ExchangeService exchangeService;
  private final Factory paperAccountServiceFactory;

  @Inject
  AccountServiceFactoryImpl(ExchangeService exchangeService,
                            OrkoConfiguration configuration,
                            PaperAccountService.Factory paperAccountServiceFactory) {
    super(configuration);
    this.exchangeService = exchangeService;
    this.paperAccountServiceFactory = paperAccountServiceFactory;
  }

  @Override
  protected ExchangeServiceFactory<AccountService> getRealFactory() {
    return exchange -> exchangeService.get(exchange).getAccountService();
  }

  @Override
  protected ExchangeServiceFactory<AccountService> getPaperFactory() {
    return paperAccountServiceFactory;
  }
}