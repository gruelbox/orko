/*
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
import {
  clearSubscriptions,
  addSubscription,
  clearJobs,
  clearOrders,
  listOrders
} from "../../support/tools"

import { NUMBER_REGEX, LONG_WAIT } from "../../util/constants"

const BITFINEX_BTC = {
  exchange: "bitfinex",
  counter: "USD",
  base: "BTC"
}

const BINANCE_BTC = {
  exchange: "binance",
  counter: "USDT",
  base: "BTC"
}

function checkCancelServerSideOrder({
  direction,
  amount,
  limitPrice,
  stopPrice
}) {
  cy.o("errorModal").should("not.exist")
  cy.o("section/jobs/tabs").within(() => {
    cy.o("all").click()
  })
  cy.o("section/jobs").within(() => {
    cy.get('[data-orko^="job/"]')
  })
  cy.o("section/orders").within(() => {
    cy.get("[data-type='openOrder/" + direction + "']").within(() => {
      cy.o("createdDate").contains("Not on exchange")
      cy.o("amount")
        .invoke("text")
        .should("eq", amount)
      cy.o("stopPrice")
        .invoke("text")
        .should("eq", stopPrice)
      cy.o("limitPrice")
        .invoke("text")
        .should("eq", limitPrice)
      cy.o("cancel")
        .invoke("width")
        .should("be.gt", 0)
      cy.o("cancel").click({ force: true })
    })
    cy.get("[data-type='openOrder/" + direction + "']", {
      timeout: LONG_WAIT
    }).should("not.exist")
  })
}

context("Trading", () => {
  beforeEach(function() {
    // Unload the site so that XHR requests overlapping setup don't
    // log the app back out again
    cy.visit("/empty.html")
    // Now start the login process
    cy.whitelist()
    cy.loginApi().then(() => {
      clearOrders(BITFINEX_BTC)
      clearOrders(BINANCE_BTC)
      clearSubscriptions().then(() => {
        addSubscription(BITFINEX_BTC)
        addSubscription(BINANCE_BTC)
      })
      clearJobs()
    })
    cy.visit("/")
    cy.o("loginModal").should("not.exist")
  })

  it("Limit orders (exchange)", () => {
    const limitTrade = (button, priceDifferential, amount) => {
      const tradePrice = tickerPrice =>
        0 + Number(tickerPrice) + priceDifferential
      cy.o("section/trading").within(() => {
        cy.o("limitOrder").within(() => {
          cy.o("limitPrice").click()
        })
      })
      cy.o("section/coinList").within(() => {
        cy.o("binance/USDT/BTC/price").contains(NUMBER_REGEX, {
          timeout: LONG_WAIT
        })
        cy.o("binance/USDT/BTC/price").click()
      })
      cy.o("limitOrder").within(() => {
        cy.o("limitPrice")
          .invoke("val")
          .as("tickerPrice")
          .then(tickerPrice => {
            cy.o("limitPrice")
              .clear()
              .type(tradePrice(tickerPrice))
          })
        cy.o("amount")
          .clear()
          .type(amount)
        cy.o(button).click()
      })
      cy.o("errorModal").should("not.exist")
      cy.get("@tickerPrice").then(tickerPrice => {
        cy.o("section/orders").within(() => {
          cy.get("[data-type='openOrder/" + button + "']").within(() => {
            cy.o("createdDate")
              .contains("Confirming...", { timeout: LONG_WAIT })
              .should("not.exist")
          })
          cy.get("[data-type='openOrder/" + button + "']", {
            timeout: LONG_WAIT
          }).within(() => {
            cy.o("amount")
              .invoke("text")
              .then(text => Number(text))
              .should("eq", amount)
            cy.o("limitPrice")
              .invoke("text")
              .then(text => Number(text))
              .should("eq", tradePrice(tickerPrice))
          })
        })
        listOrders(BINANCE_BTC).should($orders => {
          expect($orders.openOrders.length, "Open order count").to.eql(1)
          expect($orders.hiddenOrders, "Hidden orders").to.be.empty
          expect($orders.openOrders[0].limitPrice, "Limit price").to.eql(
            tradePrice(tickerPrice)
          )
          expect($orders.openOrders[0].originalAmount, "Amount").to.eql(amount)
          expect($orders.openOrders[0].type).to.eql(
            button == "buy" ? "BID" : "ASK"
          )
        })
        cy.o("section/orders").within(() => {
          cy.get("[data-type='openOrder/" + button + "']").within(() => {
            cy.o("cancel")
              .invoke("width")
              .should("be.gt", 0)
            cy.o("cancel").click({ force: true })
          })
          cy.get("[data-type='openOrder/" + button + "']", {
            timeout: LONG_WAIT
          }).should("not.exist")
          listOrders(BINANCE_BTC).should($orders => {
            expect($orders.openOrders, "Open orders").to.be.empty
            expect($orders.hiddenOrders, "Hidden orders").to.be.empty
          })
        })
      })
    }

    cy.o("section/coinList").within(() => {
      cy.o("binance/USDT/BTC/name").click()
    })
    cy.o("selectedCoin").contains("Binance")
    cy.o("selectedCoin").contains("BTC/USD")
    cy.o("section/trading/tabs").within(() => {
      cy.o("limit").click()
    })
    cy.o("section/trading").within(() => {
      cy.o("enablePaperTrading").click()
    })

    limitTrade("buy", -100, 0.1)
    limitTrade("sell", 100, 0.2)
  })

  it("Hidden orders", () => {
    const createHiddenOrder = ({
      direction,
      amount,
      highLimitPrice,
      highPrice,
      lowLimitPrice,
      lowPrice
    }) => {
      cy.o("section/trading").within(() => {
        cy.o("stopTakeProfit").within(() => {
          cy.o(direction).click()
          cy.o("highPrice").clear()
          cy.o("highLimitPrice").clear()
          cy.o("lowPrice").clear()
          cy.o("lowLimitPrice").clear()
          if (highPrice) {
            cy.o("highPrice").type(highPrice)
            cy.o("highLimitPrice").type(highLimitPrice)
          }
          if (lowPrice) {
            cy.o("lowPrice").type(lowPrice)
            cy.o("lowLimitPrice").type(lowLimitPrice)
          }
          cy.o("amount")
            .clear()
            .type(amount)
          cy.o("submitOrder").click()
        })
      })
    }

    cy.o("section/coinList").within(() => {
      cy.o("bitfinex/USD/BTC/name").click()
    })
    cy.o("selectedCoin").contains("Bitfinex")
    cy.o("selectedCoin").contains("BTC/USD")
    cy.o("section/trading/tabs").within(() => {
      cy.o("stopTakeProfit").click()
    })
    cy.o("section/trading").within(() => {
      cy.o("enablePaperTrading").click()
    })

    createHiddenOrder({
      direction: "BUY",
      highPrice: 90000,
      highLimitPrice: 100000,
      amount: 1
    })
    checkCancelServerSideOrder({
      direction: "buy",
      amount: "1",
      stopPrice: "90000",
      limitPrice: "100000"
    })

    createHiddenOrder({
      direction: "BUY",
      lowPrice: 99,
      lowLimitPrice: 100,
      amount: 1
    })
    checkCancelServerSideOrder({
      direction: "buy",
      amount: "1",
      stopPrice: "99",
      limitPrice: "100"
    })

    createHiddenOrder({
      direction: "SELL",
      lowPrice: 100,
      lowLimitPrice: 99,
      amount: 1
    })
    checkCancelServerSideOrder({
      direction: "sell",
      amount: "1",
      stopPrice: "100",
      limitPrice: "99"
    })

    createHiddenOrder({
      direction: "SELL",
      highPrice: 100000,
      highLimitPrice: 90000,
      amount: 1
    })
    checkCancelServerSideOrder({
      direction: "sell",
      amount: "1",
      stopPrice: "100000",
      limitPrice: "90000"
    })
  })

  it("Stops (server)", () => {
    cy.o("section/coinList").within(() => {
      cy.o("bitfinex/USD/BTC/name").click()
    })
    cy.o("selectedCoin").contains("Bitfinex")
    cy.o("selectedCoin").contains("BTC/USD")
    cy.o("section/trading/tabs").within(() => {
      cy.o("stop").click()
    })
    cy.o("section/trading").within(() => {
      cy.o("enablePaperTrading").click()
      cy.o("stopOrder").within(() => {
        cy.o("stopPrice").type("100")
        cy.o("amount").type("1")
        cy.get("[data-orko='onExchange'] input").uncheck({ force: true })
        cy.o("limitPrice").type("99")
        cy.o("sell").click()
      })
    })
    checkCancelServerSideOrder({
      direction: "sell",
      amount: "1",
      stopPrice: "100",
      limitPrice: "99"
    })
  })

  it("Stops (exchange)", () => {
    cy.o("section/coinList").within(() => {
      cy.o("binance/USDT/BTC/price").contains(NUMBER_REGEX, {
        timeout: 20000
      })
      cy.o("binance/USDT/BTC/name").click()
    })
    cy.o("selectedCoin").contains("Binance")
    cy.o("selectedCoin").contains("BTC/USD")
    cy.o("section/trading/tabs").within(() => {
      cy.o("stop").click()
    })
    cy.o("section/trading").within(() => {
      cy.o("enablePaperTrading").click()
      cy.o("stopOrder").within(() => {
        cy.o("stopPrice").type("1000")
        cy.o("limitPrice").click()
      })
    })
    cy.o("section/coinList").within(() => {
      cy.o("binance/USDT/BTC/price").click()
    })
    cy.o("stopOrder").within(() => {
      cy.o("limitPrice").contains(/^[0-9\\.]*/)
    })
    // TODO actually submit once paper trading supports it
    // TODO check order is displayed
  })
})
