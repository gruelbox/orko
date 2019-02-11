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
import { clearSubscriptions, addSubscription } from "../../support/tools"
import {
  NUMBER_REGEX,
  PERCENT_CHANGE_REGEX,
  LONG_WAIT
} from "../../util/constants"

context("Coins", () => {
  beforeEach(function() {
    // Unload the site so that XHR requests overlapping setup don't
    // log the app back out again
    cy.visit("/empty.html")
    // Now start the login process
    cy.whitelist()
  })

  it("Add and remove a coin", () => {
    cy.loginApi().then(() => clearSubscriptions())
    cy.visit("/")
    cy.o("loginModal").should("not.exist")
    cy.o("addCoin").click()
    cy.o("addCoinModal").within(() => {
      cy.o("selectExchange").click()
      cy.get("[class='visible menu transition']")
        .contains("Bitfinex")
        .click()
      cy.o("selectPair").click()
      cy.get("[class='visible menu transition']")
        .contains("BTC/USD")
        .click()
      cy.o("addCoinSubmit").click()
    })
    cy.o("selectedCoin").contains("Bitfinex")
    cy.o("selectedCoin").contains("BTC/USD")
    cy.o("addCoinModal").should("not.exist")
    cy.o("errorModal").should("not.exist")
    cy.o("section/coinList").within(() => {
      cy.o("bitfinex/USD/BTC/exchange").contains("Bitfinex")
      cy.o("bitfinex/USD/BTC/name").contains("BTC/USD")
      cy.o("bitfinex/USD/BTC/price", {
        timeout: LONG_WAIT
      }).shouldMatch(NUMBER_REGEX)
      cy.o("bitfinex/USD/BTC/remove")
        .invoke("width")
        .should("be.gt", 0)
      cy.o("bitfinex/USD/BTC/remove").click({ force: true })
      cy.o("bitfinex/USD/BTC/exchange").should("not.exist")
    })
  })

  it("Visit a coin directly and work with it", () => {
    cy.loginApi().then(() => {
      clearSubscriptions().then(() =>
        addSubscription({
          exchange: "binance",
          counter: "USDT",
          base: "ETH"
        })
      )
    })
    cy.visit("/coin/binance/USDT/ETH")
    cy.o("loginModal").should("not.exist")
    cy.o("selectedCoin").contains("Binance")
    cy.o("selectedCoin").contains("ETH/USD")
    cy.o("section/coinList").within(() => {
      cy.o("binance/USDT/ETH/exchange").contains("Binance")
      cy.o("binance/USDT/ETH/name").contains("ETH/USD")
      cy.o("binance/USDT/ETH/price", {
        timeout: LONG_WAIT
      })
        .shouldMatch(NUMBER_REGEX)
        .as("currentPrice")
      cy.o("binance/USDT/ETH/setReferencePrice")
        .invoke("text")
        .should("eq", "--")
      cy.o("binance/USDT/ETH/setReferencePrice").click({ force: true })
    })
    cy.o("section/referencePrice").within(() => {
      cy.get("@currentPrice").then(currentPrice =>
        cy.o("price").type(currentPrice)
      )
      cy.o("doSubmit").click()
    })
    cy.o("section/referencePrice").should("not.exist")
    cy.o("section/coinList").within(() => {
      cy.o("binance/USDT/ETH/setReferencePrice").shouldMatch(
        PERCENT_CHANGE_REGEX
      )
      cy.o("binance/USDT/ETH/setReferencePrice")
        .invoke("width")
        .should("be.gt", 0)
      cy.o("binance/USDT/ETH/setReferencePrice").click({ force: true })
    })
    cy.o("section/referencePrice").within(() => {
      cy.o("doClear").click()
    })
    cy.o("section/referencePrice").should("not.exist")
    cy.o("section/coinList").within(() => {
      cy.o("binance/USDT/ETH/setReferencePrice")
        .invoke("text")
        .should("eq", "--")
      cy.o("binance/USDT/ETH/alerts")
        .invoke("width")
        .should("be.gt", 0)
      cy.o("binance/USDT/ETH/alerts").click({ force: true })
    })
    cy.o("section/manageAlerts").within(() => {
      cy.get("@currentPrice").then(currentPrice =>
        cy.o("highPrice").type(Number(currentPrice) + 500)
      )
      cy.o("lowPrice").type("1")
      cy.o("doCreateAlert").click()
    })
    cy.o("errorModal").should("not.exist")
    cy.o("section/manageAlerts/tabs").within(() => {
      cy.o("section/manageAlerts/hide").click()
    })
    cy.o("section/manageAlerts").should("not.exist")
    cy.o("section/coinList").within(() => {
      cy.o("binance/USDT/ETH/remove")
        .invoke("width")
        .should("be.gt", 0)
      cy.o("binance/USDT/ETH/remove").click({ force: true })
      cy.o("binance/USDT/ETH/exchange").should("not.exist")
    })
  })
})
