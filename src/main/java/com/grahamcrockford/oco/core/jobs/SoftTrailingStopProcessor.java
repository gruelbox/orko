package com.grahamcrockford.oco.core.jobs;

import static java.math.RoundingMode.HALF_UP;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import javax.inject.Inject;

import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.meta.CurrencyPairMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.grahamcrockford.oco.core.ExchangeService;
import com.grahamcrockford.oco.core.JobSubmitter;
import com.grahamcrockford.oco.spi.JobProcessor;
import com.grahamcrockford.oco.spi.TickerSpec;
import com.grahamcrockford.oco.telegram.TelegramService;
import com.grahamcrockford.oco.util.Sleep;

class SoftTrailingStopProcessor implements JobProcessor<SoftTrailingStop> {

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
  private final ExchangeService exchangeService;
  private final JobSubmitter jobSubmitter;
  private final Sleep sleep;


  @Inject
  public SoftTrailingStopProcessor(final TelegramService telegramService,
                                   final ExchangeService exchangeService,
                                   final JobSubmitter jobSubmitter,
                                   final Sleep sleep) {
    this.telegramService = telegramService;
    this.exchangeService = exchangeService;
    this.jobSubmitter = jobSubmitter;
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

      jobSubmitter.submitNew(LimitSell.builder()
          .tickTrigger(ex)
          .amount(order.amount())
          .limitPrice(order.limitPrice())
          .build());

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
}