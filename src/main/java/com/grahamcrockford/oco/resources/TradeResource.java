package com.grahamcrockford.oco.resources;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.ImmutableList;
import com.grahamcrockford.oco.WebResource;
import com.grahamcrockford.oco.api.AdvancedOrder;
import com.grahamcrockford.oco.api.AdvancedOrderInfo;
import com.grahamcrockford.oco.core.ExchangeService;
import com.grahamcrockford.oco.db.QueueAccess;
import com.grahamcrockford.oco.orders.PumpChecker;

/**
 * Slightly disorganised endpoint with a mix of methods. Will get re-organised.
 */
@Path("/exchanges")
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class TradeResource implements WebResource {

  private static final Logger LOGGER = LoggerFactory.getLogger(TradeResource.class);

  private final ExchangeService exchanges;
  private final QueueAccess.Factory<AdvancedOrder> queueAccess;

  @Inject
  TradeResource(QueueAccess.Factory<AdvancedOrder> queueAccess, ExchangeService exchanges) {
    this.queueAccess = queueAccess;
    this.exchanges = exchanges;
  }

  @GET
  @Timed
  public List<String> list() {
    return ImmutableList.of("binance");
  }

  @GET
  @Path("{exchange}/{counter}/{base}/ticker")
  @Timed
  public Ticker ticker(@PathParam("exchange") String exchange,
                       @PathParam("counter") String counter,
                       @PathParam("base") String base) throws IOException {
    return exchanges.get(exchange).getMarketDataService().getTicker(new CurrencyPair(base, counter));
  }

//  @DELETE
//  @Path("{exchange}/{counter}/{base}/jobs/{id}")
//  @Timed
//  public void deleteJob(@PathParam("id") long id) {
//    manager.deleteJob(id);
//  }
//
//
//  @GET
//  @Path("{exchange}/{counter}/{base}/jobs/{id}")
//  @Timed
//  public AdvancedOrder getJob(@PathParam("id") long id) {
//    return manager.getJob(id);
//  }
//
//  @GET
//  @Path("{exchange}/{counter}/{base}/jobs/create/monitorlasttrade")
//  @Timed
//  public OrderStateNotifier monitorLastTrade(@PathParam("exchange") String exchange,
//                                                     @PathParam("counter") String counter,
//                                                     @PathParam("base") String base) throws Exception {
//
//    OrderStateNotifier build = OrderStateNotifier.builder()
//    .id(System.currentTimeMillis())
//    .basic(AdvancedOrderInfo.builder()
//      .exchange(exchange)
//      .base(base)
//      .counter(counter)
//      .build()
//    )
//    .build();
//    queueAccess.submit(build);
//  }

//  @GET
//  @Path("{exchange}/{counter}/{base}/jobs/create/softtrailingstop")
//  @Timed
//  public SoftTrailingStop softTrailingStop(@PathParam("exchange") String exchange,
//                                                   @PathParam("counter") String counter,
//                                                   @PathParam("base") String base,
//                                                   @QueryParam("a") BigDecimal amount,
//                                                   @QueryParam("s") BigDecimal stopPercentage,
//                                                   @QueryParam("l") BigDecimal limitPercentage) throws Exception {
//    final Ticker ticker = ticker(exchange, counter, base);
//    return queueAccess.submit(
//      SoftTrailingStop.builder()
//        .id(manager.newJobId())
//        .basic(AdvancedOrderInfo.builder()
//          .exchange(exchange)
//          .base(base)
//          .counter(counter)
//          .build()
//        )
//        .amount(amount)
//        .startPrice(ticker.getBid())
//        .stopPercentage(stopPercentage)
//        .limitPercentage(limitPercentage)
//        .build()
//    );
//  }


  @GET
  @Path("{exchange}/{counter}/{base}/jobs/create/pumpchecker")
  @Timed
  public PumpChecker pumpChecker(@PathParam("exchange") String exchange,
                                 @PathParam("counter") String counter,
                                 @PathParam("base") String base) throws Exception {
    PumpChecker job = PumpChecker.builder()
        .id(System.currentTimeMillis())
        .basic(AdvancedOrderInfo.builder()
          .exchange(exchange)
          .base(base)
          .counter(counter)
          .build()
        )
        .build();
    try (QueueAccess<AdvancedOrder> access = queueAccess.create()) {
      access.submit(job, Long.toString(job.id()));
      LOGGER.info("Submitted " + job);
    }
    return job;
  }


//  @GET
//  @Path("{exchange}/{counter}/{base}/jobs/create/oco")
//  @Timed
//  public OneCancelsOther generateOneCancelsOther(@PathParam("exchange") String exchange,
//                                                 @PathParam("counter") String counter,
//                                                 @PathParam("base") String base,
//                                                 @QueryParam("a") BigDecimal amount,
//                                                 @QueryParam("s") BigDecimal stopPercentage,
//                                                 @QueryParam("l") BigDecimal limitPercentage,
//                                                 @QueryParam("c") BigDecimal capitalisePercentage,
//                                                 @Context UriInfo info) throws Exception {
//    final Ticker ticker = ticker(exchange, counter, base);
//
//    final String u = info.getQueryParameters().getFirst("u");
//    final boolean trailingUp = u == null ? false : Boolean.valueOf(u);
//
//    final String d = info.getQueryParameters().getFirst("d");
//    final boolean reEntering = d == null ? false : Boolean.valueOf(u);
//
//    final OneCancelsOther order = OneCancelsOther
//      .onExchange(exchange)
//      .onCurrencyPair(base, counter)
//      .units(amount)
//      .atPrice(ticker.getAsk())
//      .stopIfDropsByPercent(stopPercentage)
//      .stopLimitedToPercent(limitPercentage)
//      .capitaliseAt(capitalisePercentage)
//      .trailingStop(trailingUp)
//      .reEnterAfterStopAtEntryPrice(reEntering)
//      .build();
//
//    oneCancelsOtherProcessor.limitBuy(order, ticker);
//
//    return order;
//  }
}