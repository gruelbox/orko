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

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.dto.account.AccountInfo;
import org.knowm.xchange.dto.account.FundingRecord;
import org.knowm.xchange.exceptions.NotAvailableFromExchangeException;
import org.knowm.xchange.service.account.AccountService;
import org.knowm.xchange.service.trade.params.TradeHistoryParams;
import org.knowm.xchange.service.trade.params.WithdrawFundsParams;

import com.google.inject.Inject;
import com.gruelbox.orko.OrkoConfiguration;

class AccountServiceFactoryImpl implements AccountServiceFactory {

  private final ExchangeService exchangeService;
  private final OrkoConfiguration configuration;
  private final AccountService dummyService;

  @Inject
  AccountServiceFactoryImpl(ExchangeService exchangeService, OrkoConfiguration configuration) {
    this.exchangeService = exchangeService;
    this.configuration = configuration;
    this.dummyService = new AccountService() {

      @Override
      public String withdrawFunds(Currency currency, BigDecimal amount, String address) throws IOException {
        throw new NotAvailableFromExchangeException();
      }

      @Override
      public String withdrawFunds(WithdrawFundsParams params) throws IOException {
        throw new NotAvailableFromExchangeException();
      }

      @Override
      public String requestDepositAddress(Currency currency, String... args) throws IOException {
        throw new NotAvailableFromExchangeException();
      }

      @Override
      public List<FundingRecord> getFundingHistory(TradeHistoryParams params) throws IOException {
        throw new NotAvailableFromExchangeException();
      }

      @Override
      public AccountInfo getAccountInfo() throws IOException {
        throw new NotAvailableFromExchangeException();
      }

      @Override
      public TradeHistoryParams createFundingHistoryParams() {
        throw new NotAvailableFromExchangeException();
      }
    };
  }

  @Override
  public AccountService getForExchange(String exchange) {
    Map<String, ExchangeConfiguration> exchangeConfig = configuration.getExchanges();
    if (exchangeConfig == null) {
      return dummyService;
    }
    final ExchangeConfiguration exchangeConfiguration = configuration.getExchanges().get(exchange);
    if (exchangeConfiguration == null || StringUtils.isEmpty(exchangeConfiguration.getApiKey())) {
      return dummyService;
    }
    return exchangeService.get(exchange).getAccountService();
  }
}