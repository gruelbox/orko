context("Coins", () => {
  beforeEach(function() {
    cy.whitelist()
    cy.login()
  })

  it("Add a coin", () => {
    cy.get("[data-orko=addCoin]").click()
    cy.get("[data-orko=selectExchange]").click()
    cy.get("[class='visible menu transition']")
      .contains("gdax")
      .click()
    cy.get("[data-orko=selectPair]").click()
    cy.get("[class='visible menu transition']")
      .contains("BTC/USD")
      .click()
    cy.get("[data-orko=submit]").click()
  })
})
