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

import {
  NUMBER_REGEX,
  LONG_WAIT,
  BINANCE_ETH,
  BINANCE_BTC
} from "../../util/constants"

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
    cy.get('[data-orko^="job/"]', {
      timeout: LONG_WAIT
    })
  })
  cy.o("section/orders").within(() => {
    cy.get("[data-type='openOrder/" + direction + "']").within(() => {
      cy.o("createdDate").contains("Not on exchange")
      cy.o("amount")
        .invoke("text")
        .then(text =>
          expect(parseFloat(text).toFixed(2)).to.equal(
            parseFloat(amount).toFixed(2)
          )
        )
      cy.o("stopPrice")
        .invoke("text")
        .then(text =>
          expect(parseFloat(text).toFixed(2)).to.equal(
            parseFloat(stopPrice).toFixed(2)
          )
        )
      cy.o("limitPrice")
        .invoke("text")
        .then(text =>
          expect(parseFloat(text).toFixed(2)).to.equal(
            parseFloat(limitPrice).toFixed(2)
          )
        )
      cy.o("cancel").safeClick()
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
      clearOrders(BINANCE_ETH)
      clearOrders(BINANCE_BTC)
      clearSubscriptions().then(() => {
        addSubscription(BINANCE_ETH)
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
        (0 + Number(tickerPrice) + priceDifferential).toFixed(2)
      cy.o("section/trading").within(() => {
        cy.o("limitOrder").within(() => {
          cy.o("limitPrice").click()
        })
      })
      cy.o("section/coinList").within(() => {
        cy.o("binance/USDT/BTC/price")
          .contains(NUMBER_REGEX, {
            timeout: LONG_WAIT
          })
          .click()
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
              .should("eq", amount)
            cy.o("limitPrice")
              .invoke("text")
              .should("eq", tradePrice(tickerPrice))
          })
        })
        listOrders(BINANCE_BTC).should($orders => {
          expect($orders.openOrders.length, "Open order count").to.eql(1)
          expect($orders.hiddenOrders, "Hidden orders").to.be.empty
          expect(
            $orders.openOrders[0].limitPrice.toFixed(2),
            "Limit price"
          ).to.eql(tradePrice(tickerPrice))
          expect(
            $orders.openOrders[0].originalAmount.toFixed(2),
            "Amount"
          ).to.eql(amount)
          expect($orders.openOrders[0].type).to.eql(
            button == "buy" ? "BID" : "ASK"
          )
        })
        cy.o("section/orders").within(() => {
          cy.get("[data-type='openOrder/" + button + "']").within(() => {
            cy.o("cancel").safeClick()
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

    limitTrade("buy", -100, "0.01")
    limitTrade("sell", 100, "0.02")
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
      cy.o("binance/USDT/BTC/name").click()
      cy.o("binance/USDT/BTC/price")
        .contains(NUMBER_REGEX, {
          timeout: LONG_WAIT
        })
        .invoke("text")
        .as("price")
    })
    cy.o("selectedCoin").contains("Binance")
    cy.o("selectedCoin").contains("BTC/USDT")
    cy.o("section/trading/tabs").within(() => {
      cy.o("stopTakeProfit").click()
    })
    cy.o("section/trading").within(() => {
      cy.o("enablePaperTrading").click()
    })
    cy.get("@price").then(priceText => {
      const price = Number(priceText)

      createHiddenOrder({
        direction: "BUY",
        highPrice: price + 1000,
        highLimitPrice: price + 1100,
        amount: 1
      })
      checkCancelServerSideOrder({
        direction: "buy",
        amount: "1",
        stopPrice: price + 1000,
        limitPrice: price + 1100
      })

      createHiddenOrder({
        direction: "BUY",
        lowPrice: price - 900,
        lowLimitPrice: price - 1000,
        amount: 1
      })
      checkCancelServerSideOrder({
        direction: "buy",
        amount: "1",
        stopPrice: price - 900,
        limitPrice: price - 1000
      })

      createHiddenOrder({
        direction: "SELL",
        lowPrice: price - 1000,
        lowLimitPrice: price - 900,
        amount: 1
      })
      checkCancelServerSideOrder({
        direction: "sell",
        amount: "1",
        stopPrice: price - 1000,
        limitPrice: price - 900
      })

      createHiddenOrder({
        direction: "SELL",
        highPrice: price + 1100,
        highLimitPrice: price + 1000,
        amount: 1
      })
      checkCancelServerSideOrder({
        direction: "sell",
        amount: "1",
        stopPrice: price + 1100,
        limitPrice: price + 1000
      })
    })
  })

  it("Stops (server)", () => {
    cy.o("section/coinList").within(() => {
      cy.o("binance/USDT/BTC/name").click()
      cy.o("binance/USDT/BTC/price")
        .contains(NUMBER_REGEX, {
          timeout: LONG_WAIT
        })
        .invoke("text")
        .as("price")
    })
    cy.o("selectedCoin").contains("Binance")
    cy.o("selectedCoin").contains("BTC/USDT")
    cy.o("section/trading/tabs").within(() => {
      cy.o("stop").click()
    })
    cy.get("@price").then(priceText => {
      const price = Number(priceText)
      const stopPrice = price - 1000
      const limitPrice = price - 1100

      cy.o("section/trading").within(() => {
        cy.o("enablePaperTrading").click()
        cy.o("stopOrder").within(() => {
          cy.o("stopPrice").type(stopPrice)
          cy.o("amount").type("1")
          cy.get("[data-orko='onExchange'] input").uncheck({ force: true })
          cy.o("limitPrice").type(limitPrice)
          cy.o("sell").click()
        })
      })

      checkCancelServerSideOrder({
        direction: "sell",
        amount: "1",
        stopPrice,
        limitPrice
      })
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
