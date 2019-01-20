package com.gruelbox.orko.exchange;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.dto.account.AccountInfo;
import org.knowm.xchange.dto.account.Balance;
import org.knowm.xchange.dto.account.FundingRecord;
import org.knowm.xchange.dto.account.Wallet;
import org.knowm.xchange.exceptions.NotAvailableFromExchangeException;
import org.knowm.xchange.service.account.AccountService;
import org.knowm.xchange.service.trade.params.TradeHistoryParams;
import org.knowm.xchange.service.trade.params.WithdrawFundsParams;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.FluentIterable;
import com.google.inject.Inject;
import com.google.inject.Singleton;

class PaperAccountService implements AccountService {

  private static final BigDecimal INITIAL_BALANCE = new BigDecimal(1000);

  private final ConcurrentMap<Currency, AtomicReference<BigDecimal>> balances;
  private final String exchange;

  PaperAccountService(String exchange, Set<Currency> currencies) {
    this.exchange = exchange;
    this.balances = new ConcurrentHashMap<>(FluentIterable.from(currencies).toMap(k -> new AtomicReference<>(INITIAL_BALANCE)));
  }

  @Override
  public AccountInfo getAccountInfo() throws IOException {
    List<Balance> balanceList = balances.entrySet().stream()
      .map(e -> new Balance(e.getKey(), e.getValue().get()))
      .collect(toList());
    if (exchange.equals(Exchanges.BITFINEX)) {
      return new AccountInfo(
        new Wallet("margin", Collections.emptyList()),
        new Wallet("funding", Collections.emptyList()),
        new Wallet("exchange", balanceList)
      );
    } else {
      return new AccountInfo(
        new Wallet(balanceList)
      );
    }
  }

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
  public TradeHistoryParams createFundingHistoryParams() {
    throw new NotAvailableFromExchangeException();
  }

  BigDecimal incrementAndGet(String currency, BigDecimal amount) {
    AtomicReference<BigDecimal> existing = balances.computeIfAbsent(
      Currency.getInstance(currency),
      k -> new AtomicReference<>(INITIAL_BALANCE)
    );
    return existing.accumulateAndGet(amount, (x, y) -> x.add(y));
  }

  @Singleton
  public static class Factory implements AccountServiceFactory {

    private final ExchangeService exchangeService;

    private final LoadingCache<String, PaperAccountService> services = CacheBuilder.newBuilder().initialCapacity(1000).build(new CacheLoader<String, PaperAccountService>() {
      @Override
      public PaperAccountService load(String exchangeName) throws Exception {
        return new PaperAccountService(exchangeName, exchangeService.get(exchangeName).getExchangeMetaData().getCurrencies().keySet());
      }
    });

    @Inject
    Factory(ExchangeService exchangeService) {
      this.exchangeService = exchangeService;
    }

    @Override
    public PaperAccountService getForExchange(String exchange) {
      return services.getUnchecked(exchange);
    }
  }
}
