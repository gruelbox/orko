package com.grahamcrockford.oco.core.jobs;

import static java.math.RoundingMode.HALF_UP;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.Optional;
import javax.inject.Inject;

import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.meta.CurrencyPairMetaData;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.service.trade.TradeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Singleton;
import com.grahamcrockford.oco.api.JobProcessor;
import com.grahamcrockford.oco.api.TickerSpec;
import com.grahamcrockford.oco.core.ExchangeService;
import com.grahamcrockford.oco.core.TelegramService;
import com.grahamcrockford.oco.core.TradeServiceFactory;
import com.grahamcrockford.oco.db.JobAccess;
import com.grahamcrockford.oco.util.Sleep;

@Singleton
public class SoftTrailingStopProcessor implements JobProcessor<SoftTrailingStop> {

  private static final Logger LOGGER = LoggerFactory.getLogger(SoftTrailingStopProcessor.class);
  private static final ColumnLogger COLUMN_LOGGER = new ColumnLogger(LOGGER,
      LogColumn.builder().name("#").width(26).rightAligned(false),
      LogColumn.builder().name("Exchange").width(12).rightAligned(false),
      LogColumn.builder().name("Pair").width(10).rightAligned(false),
      LogColumn.builder().name("Operation").width(13).rightAligned(false),
      LogColumn.builder().name("Entry").width(13).rightAligned(true),
      LogColumn.builder().name("Stop").width(13).rightAligned(true),
      LogColumn.builder().name("Bid").width(13).rightAligned(true),
      LogColumn.builder().name("Last").width(13).rightAligned(true),
      LogColumn.builder().name("Ask").width(13).rightAligned(true)
  );

  private final TelegramService telegramService;
  private final TradeServiceFactory tradeServiceFactory;
  private final ExchangeService exchangeService;
  private final JobAccess advancedOrderAccess;
  private final Sleep sleep;


  @Inject
  public SoftTrailingStopProcessor(final TelegramService telegramService,
                                   final TradeServiceFactory tradeServiceFactory,
                                   final ExchangeService exchangeService,
                                   final JobAccess advancedOrderAccess,
                                   final Sleep sleep) {
    this.telegramService = telegramService;
    this.tradeServiceFactory = tradeServiceFactory;
    this.exchangeService = exchangeService;
    this.advancedOrderAccess = advancedOrderAccess;
    this.sleep = sleep;
  }

  @Override
  public Optional<SoftTrailingStop> process(final SoftTrailingStop order) throws InterruptedException {

    final TickerSpec ex = order.tickTrigger();

    Ticker ticker = exchangeService.fetchTicker(ex);

    if (ticker.getAsk() == null) {
      LOGGER.warn("Market {}/{}/{} has no sellers!", ex.exchange(), ex.base(), ex.counter());
      telegramService.sendMessage(String.format("Market %s/%s/%s has no sellers!", ex.exchange(), ex.base(), ex.counter()));
      return Optional.of(order);
    }
    if (ticker.getBid() == null) {
      LOGGER.warn("Market {}/{}/{} has no buyers!", ex.exchange(), ex.base(), ex.counter());
      telegramService.sendMessage(String.format("Market %s/%s/%s has no buyers!", ex.exchange(), ex.base(), ex.counter()));
      return Optional.of(order);
    }

    final CurrencyPairMetaData currencyPairMetaData = exchangeService.fetchCurrencyPairMetaData(ex);

    logStatus(order, ticker, currencyPairMetaData);

    // If we've hit the stop price, we're done
    if (ticker.getBid().compareTo(stopPrice(order, currencyPairMetaData)) <= 0) {

      // Sell up
      String xChangeOrderId = limitSell(order, ticker, limitPrice(order, currencyPairMetaData));

      // Spawn a new job to monitor the progress of the stop
      advancedOrderAccess.insert(OrderStateNotifier.builder()
          .tickTrigger(order.tickTrigger())
          .description("Stop")
          .orderId(xChangeOrderId)
          .build(), OrderStateNotifier.class);

      return Optional.empty();
    }

    SoftTrailingStop newOrder;

    if (ticker.getBid().compareTo(order.lastSyncPrice()) > 0 ) {
      newOrder = order.toBuilder()
        .lastSyncPrice(ticker.getBid())
        .stopPrice(order.stopPrice().add(ticker.getBid()).subtract(order.lastSyncPrice()))
        .build();
    } else {
      newOrder = order;
    }

    sleep.sleep();
    return Optional.of(newOrder);
  }

  private String limitSell(SoftTrailingStop trailingStop, Ticker ticker, BigDecimal limitPrice) {
    final TickerSpec ex = trailingStop.tickTrigger();

    LOGGER.info("| - Placing limit sell of [{} {}] at limit price [{} {}]", trailingStop.amount(), ex.base(), limitPrice, ex.counter());
    final TradeService tradeService = tradeServiceFactory.getForExchange(ex.exchange());
    final Date timestamp = ticker.getTimestamp() == null ? new Date() : ticker.getTimestamp();
    final LimitOrder order = new LimitOrder(Order.OrderType.ASK, trailingStop.amount(), ex.currencyPair(), null, timestamp, limitPrice);

    try {
      String xChangeOrderId = tradeService.placeLimitOrder(order);

      LOGGER.info("| - Order [{}] placed.", xChangeOrderId);
      telegramService.sendMessage(String.format(
        "Job [%s] on [%s/%s/%s] placed limit sell at [%s]",
        trailingStop.id(),
        ex.exchange(),
        ex.base(),
        ex.counter(),
        limitPrice
      ));

      return xChangeOrderId;

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }


  private void logStatus(final SoftTrailingStop trailingStop, final Ticker ticker, CurrencyPairMetaData currencyPairMetaData) {
    final TickerSpec ex = trailingStop.tickTrigger();
    COLUMN_LOGGER.line(
      trailingStop.id(),
      ex.exchange(),
      ex.pairName(),
      "Trailing stop",
      trailingStop.startPrice().setScale(currencyPairMetaData.getPriceScale(), HALF_UP),
      stopPrice(trailingStop, currencyPairMetaData),
      ticker.getBid().setScale(currencyPairMetaData.getPriceScale(), HALF_UP),
      ticker.getLast().setScale(currencyPairMetaData.getPriceScale(), HALF_UP),
      ticker.getAsk().setScale(currencyPairMetaData.getPriceScale(), HALF_UP)
    );
  }

  private BigDecimal stopPrice(SoftTrailingStop trailingStop, CurrencyPairMetaData currencyPairMetaData) {
    return trailingStop.stopPrice().setScale(currencyPairMetaData.getPriceScale(), RoundingMode.HALF_UP);
  }

  private BigDecimal limitPrice(SoftTrailingStop trailingStop, CurrencyPairMetaData currencyPairMetaData) {
    return trailingStop.limitPrice().setScale(currencyPairMetaData.getPriceScale(), RoundingMode.HALF_UP);
  }
}