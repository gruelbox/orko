package com.grahamcrockford.oco.core.jobs;

import static java.math.RoundingMode.HALF_UP;

import java.math.BigDecimal;
import java.math.RoundingMode;
import javax.inject.Inject;

import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.meta.CurrencyPairMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.grahamcrockford.oco.core.api.ExchangeEventRegistry;
import com.grahamcrockford.oco.core.api.ExchangeService;
import com.grahamcrockford.oco.core.api.JobSubmitter;
import com.grahamcrockford.oco.core.jobs.LimitOrderJob.Direction;
import com.grahamcrockford.oco.core.spi.JobControl;
import com.grahamcrockford.oco.core.spi.JobProcessor;
import com.grahamcrockford.oco.core.spi.TickerSpec;
import com.grahamcrockford.oco.telegram.TelegramService;

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
  private final SoftTrailingStop job;
  private final JobControl jobControl;
  private final ExchangeEventRegistry exchangeEventRegistry;


  @Inject
  public SoftTrailingStopProcessor(@Assisted SoftTrailingStop job,
                                   @Assisted JobControl jobControl,
                                   final TelegramService telegramService,
                                   final ExchangeService exchangeService,
                                   final JobSubmitter jobSubmitter,
                                   final ExchangeEventRegistry exchangeEventRegistry) {
    this.job = job;
    this.jobControl = jobControl;
    this.telegramService = telegramService;
    this.exchangeService = exchangeService;
    this.jobSubmitter = jobSubmitter;
    this.exchangeEventRegistry = exchangeEventRegistry;
  }

  @Override
  public boolean start() {
    exchangeEventRegistry.registerTicker(job.tickTrigger(), job.id(), this::tick);
    return true;
  }

  @Override
  public void stop() {
    exchangeEventRegistry.unregisterTicker(job.tickTrigger(), job.id());
  }

  @VisibleForTesting
  void tick(Ticker ticker) {

    final TickerSpec ex = job.tickTrigger();

    if (ticker.getAsk() == null) {
      LOGGER.warn("Market {}/{}/{} has no sellers!", ex.exchange(), ex.base(), ex.counter());
      telegramService.sendMessage(String.format("Market %s/%s/%s has no sellers!", ex.exchange(), ex.base(), ex.counter()));
      return;
    }
    if (ticker.getBid() == null) {
      LOGGER.warn("Market {}/{}/{} has no buyers!", ex.exchange(), ex.base(), ex.counter());
      telegramService.sendMessage(String.format("Market %s/%s/%s has no buyers!", ex.exchange(), ex.base(), ex.counter()));
      return;
    }

    final CurrencyPairMetaData currencyPairMetaData = exchangeService.fetchCurrencyPairMetaData(ex);

    logStatus(job, ticker, currencyPairMetaData);

    // If we've hit the stop price, we're done
    if ((job.direction().equals(Direction.SELL) && ticker.getBid().compareTo(stopPrice(job, currencyPairMetaData)) <= 0) ||
        (job.direction().equals(Direction.BUY) && ticker.getAsk().compareTo(stopPrice(job, currencyPairMetaData)) >= 0)) {

      jobSubmitter.submitNew(LimitOrderJob.builder()
          .tickTrigger(ex)
          .direction(job.direction())
          .amount(job.amount())
          .limitPrice(job.limitPrice())
          .build());

      jobControl.finish();
      return;
    }

    if (job.direction().equals(Direction.SELL) && ticker.getBid().compareTo(job.lastSyncPrice()) > 0) {
      jobControl.replace(
        job.toBuilder()
          .lastSyncPrice(ticker.getBid())
          .stopPrice(job.stopPrice().add(ticker.getBid()).subtract(job.lastSyncPrice()))
          .build()
      );
    }

    if (job.direction().equals(Direction.BUY) && ticker.getAsk().compareTo(job.lastSyncPrice()) < 0 ) {
      jobControl.replace(
        job.toBuilder()
          .lastSyncPrice(ticker.getAsk())
          .stopPrice(job.stopPrice().subtract(ticker.getAsk()).add(job.lastSyncPrice()))
          .build()
      );
    }
  }

  private void logStatus(final SoftTrailingStop trailingStop, final Ticker ticker, CurrencyPairMetaData currencyPairMetaData) {
    final TickerSpec ex = trailingStop.tickTrigger();
    COLUMN_LOGGER.line(
      trailingStop.id(),
      ex.exchange(),
      ex.pairName(),
      "Trailing " + trailingStop.direction(),
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

  public interface Factory extends JobProcessor.Factory<SoftTrailingStop> { }

  public static final class Module extends AbstractModule {
    @Override
    protected void configure() {
      install(new FactoryModuleBuilder()
          .implement(new TypeLiteral<JobProcessor<SoftTrailingStop>>() {}, SoftTrailingStopProcessor.class)
          .build(Factory.class));
    }
  }
}