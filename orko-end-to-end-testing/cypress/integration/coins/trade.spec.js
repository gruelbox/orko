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
  clearOrders
} from "../../support/tools"

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

context("Trading", () => {
  beforeEach(function() {
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
  })

  it("Limit: on exchange", () => {
    cy.o("section/coinList").within(() => {
      cy.o("binance/USDT/BTC/price").contains(/^[0-9\.]*/, {
        timeout: 20000
      })
      cy.o("binance/USDT/BTC/name").click()
    })
    cy.o("selectedCoin").contains("Binance")
    cy.o("selectedCoin").contains("BTC/USD")
    cy.o("section/trading/tabs").within(() => {
      cy.o("limit").click()
    })
    cy.o("section/trading").within(() => {
      cy.o("enablePaperTrading").click()
      cy.o("limitOrder").within(() => {
        cy.o("limitPrice").click()
      })
    })
    cy.o("section/coinList").within(() => {
      cy.o("binance/USDT/BTC/price").click()
    })
    cy.o("limitOrder").within(() => {
      cy.o("limitPrice").contains(/^[\0-9\.]*/)
      cy.o("limitPrice")
        .invoke("text")
        .then(text => cy.o("limitPrice").type(Number(text) + 100))
      cy.o("amount").type("0.2")
      cy.o("sell").click()
    })
    // TODO check order is displayed
  })

  it("Stop: on exchange", () => {
    cy.o("section/coinList").within(() => {
      cy.o("binance/USDT/BTC/price").contains(/^[0-9\.]*/, {
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
      cy.o("limitPrice").contains(/^[\0-9\.]*/)
    })
    // TODO actually submit once paper trading supports it
    // TODO check order is displayed
  })

  it("OCO: Buy only", () => {
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
      cy.o("stopTakeProfit").within(() => {
        cy.o("BUY").click()
        cy.o("lowPrice").type("1000")
        cy.o("lowLimitPrice").type("1100")
        cy.o("amount").type("1")
        cy.o("submitOrder").click()
      })
    })
    cy.o("section/jobs/tabs").within(() => {
      cy.o("all").click()
    })
    cy.o("section/jobs").within(() => {
      cy.get('[data-orko^="job/"]')
    })
    cy.o("errorModal").should("not.exist")

    // TODO verify text
    // TODO check order is displayed
  })
})
