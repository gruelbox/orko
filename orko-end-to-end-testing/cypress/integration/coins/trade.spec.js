import {
  clearSubscriptions,
  addSubscription,
  clearJobs
} from "../../support/tools"

context("Trading", () => {
  beforeEach(function() {
    cy.whitelist()
    cy.loginApi().then(() => {
      clearSubscriptions().then(() =>
        addSubscription({
          exchange: "bitfinex",
          counter: "USD",
          base: "BTC"
        })
      )
      clearJobs()
    })
    cy.visit("/")
  })

  it("Server-side buy order", () => {
    cy.o("section/coinList").within(() => {
      cy.o("bitfinex/USD/BTC/name").click()
    })
    cy.o("selectedCoin").contains("bitfinex")
    cy.o("selectedCoin").contains("BTC/USD")
    cy.o("section/trading/tabs").within(() => {
      cy.o("stopTakeProfit").click()
    })
    cy.o("section/trading").within(() => {
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
    // TODO verify that the job has appeared in the trades list
  })
})
