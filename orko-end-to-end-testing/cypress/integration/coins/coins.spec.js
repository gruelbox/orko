import { clearSubscriptions, addSubscription } from "../../support/tools"

context("Coins", () => {
  it("Add and remove a coin", () => {
    cy.whitelist()
    cy.loginApi().then(() => clearSubscriptions())
    cy.visit("/")
    cy.o("addCoin").click()
    cy.o("addCoinModal").within(() => {
      cy.o("selectExchange").click()
      cy.get("[class='visible menu transition']")
        .contains("bitfinex")
        .click()
      cy.o("selectPair").click()
      cy.get("[class='visible menu transition']")
        .contains("BTC/USD")
        .click()
      cy.o("addCoinSubmit").click()
    })
    cy.o("selectedCoin").contains("bitfinex")
    cy.o("selectedCoin").contains("BTC/USD")
    cy.o("addCoinModal").should("not.exist")
    cy.o("errorModal").should("not.exist")
    cy.o("section/coinList").within(() => {
      cy.o("bitfinex/USD/BTC/exchange").contains("bitfinex")
      cy.o("bitfinex/USD/BTC/name").contains("BTC/USD")
      cy.o("bitfinex/USD/BTC/price").contains(/^[0-9\.]*/)
      cy.o("bitfinex/USD/BTC/remove").click()
      cy.o("bitfinex/USD/BTC/exchange").should("not.exist")
    })
  })

  it("Visit a coin directly", () => {
    cy.whitelist()
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
    cy.o("selectedCoin").contains("binance")
    cy.o("selectedCoin").contains("ETH/USD")
    cy.o("section/coinList").within(() => {
      cy.o("binance/USDT/ETH/exchange").contains("binance")
      cy.o("binance/USDT/ETH/name").contains("ETH/USD")
      cy.o("binance/USDT/ETH/price").contains(/^[0-9\.]*/)
      cy.o("binance/USDT/ETH/remove").click()
      cy.o("binance/USDT/ETH/exchange").should("not.exist")
    })
  })
})
