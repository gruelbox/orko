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

import java.util.Collection;

import org.knowm.xchange.Exchange;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.meta.CurrencyPairMetaData;

import com.google.inject.ImplementedBy;
import com.gruelbox.orko.spi.TickerSpec;

@ImplementedBy(ExchangeServiceImpl.class)
public interface ExchangeService {

  Collection<String> getExchanges();

  Exchange get(String name);

  boolean isAuthenticated(String name);

  CurrencyPairMetaData fetchCurrencyPairMetaData(TickerSpec ex);

  RateController rateController(String name);

  boolean exchangeSupportsPair(String exchange, CurrencyPair currencyPair);

}