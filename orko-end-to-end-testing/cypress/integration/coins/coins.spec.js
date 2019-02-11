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
  clearOrders,
  clearJobs
} from "../../support/tools"
import {
  NUMBER_REGEX,
  PERCENT_CHANGE_REGEX,
  LONG_WAIT,
  BINANCE_ETH,
  BINANCE_BTC
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
    cy.loginApi().then(() => {
      clearOrders(BINANCE_BTC)
      clearJobs()
      clearSubscriptions()
    })
    cy.visit("/")
    cy.o("loginModal").should("not.exist")
    cy.o("addCoin").click()
    cy.o("addCoinModal").within(() => {
      cy.o("selectExchange").click()
      cy.get("[class='visible menu transition']")
        .contains("Binance")
        .click()
      cy.o("selectPair").click()
      cy.get("[class='visible menu transition']")
        .contains("BTC/USDT")
        .click()
      cy.o("addCoinSubmit").click()
    })
    cy.o("selectedCoin").contains("Binance")
    cy.o("selectedCoin").contains("BTC/USDT")
    cy.o("addCoinModal").should("not.exist")
    cy.o("errorModal").should("not.exist")
    cy.o("section/coinList").within(() => {
      cy.o("binance/USDT/BTC/exchange").contains("Binance")
      cy.o("binance/USDT/BTC/name").contains("BTC/USDT")
      cy.o("binance/USDT/BTC/price").contains(NUMBER_REGEX, {
        timeout: LONG_WAIT
      })
      cy.o("binance/USDT/BTC/remove").safeClick()
      cy.o("binance/USDT/BTC/exchange").should("not.exist")
    })
  })

  it("Visit a coin directly and work with it", () => {
    cy.loginApi().then(() => {
      clearOrders(BINANCE_ETH)
      clearOrders(BINANCE_BTC)
      clearJobs()
      clearSubscriptions().then(() => addSubscription(BINANCE_ETH))
    })
    cy.visit("/coin/binance/USDT/ETH")
    cy.o("loginModal").should("not.exist")
    cy.o("selectedCoin").contains("Binance")
    cy.o("selectedCoin").contains("ETH/USD")
    cy.o("section/coinList").within(() => {
      cy.o("binance/USDT/ETH/exchange").contains("Binance")
      cy.o("binance/USDT/ETH/name").contains("ETH/USD")
      cy.o("binance/USDT/ETH/price")
        .contains(NUMBER_REGEX, {
          timeout: LONG_WAIT
        })
        .invoke("text")
        .as("currentPrice")
      cy.o("binance/USDT/ETH/setReferencePrice")
        .should("have.text", "--")
        .safeClick()
    })
    cy.o("section/referencePrice").within(() => {
      cy.get("@currentPrice").then(currentPrice =>
        cy.o("price").type(currentPrice)
      )
      cy.o("doSubmit").click()
    })
    cy.o("section/referencePrice").should("not.exist")
    cy.o("section/coinList").within(() => {
      //This almost always fails on headless Chrome
      //and I can't work out why. This pause seems
      //to resolve it.
      cy.wait(1000)
      cy.o("binance/USDT/ETH/setReferencePrice")
        .invoke("text")
        .should("match", PERCENT_CHANGE_REGEX)
      cy.o("binance/USDT/ETH/setReferencePrice").safeClick()
    })
    cy.o("section/referencePrice").within(() => {
      cy.o("doClear").click()
    })
    cy.o("section/referencePrice").should("not.exist")
    cy.o("section/coinList").within(() => {
      cy.o("binance/USDT/ETH/setReferencePrice").should("have.text", "--")
      cy.o("binance/USDT/ETH/alerts").safeClick()
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
      cy.o("binance/USDT/ETH/remove").safeClick()
      cy.o("binance/USDT/ETH/exchange").should("not.exist")
    })
  })
})
