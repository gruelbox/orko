context("Coins", () => {
  beforeEach(function() {
    cy.whitelist()
    cy.loginApi()
      .its("body")
      .then(auth => {
        const options = {
          url: "/api/subscriptions",
          headers: {
            "x-xsrf-token": auth.xsrf
          }
        }
        cy.request({
          ...options,
          method: "GET"
        }).should(response => {
          expect(response.status).to.eq(200)
          expect(response.body).to.be.an.array
          response.body.forEach(ticker => {
            cy.request({
              ...options,
              method: "DELETE",
              body: ticker
            })
          })
        })
      })
    cy.login()
  })

  it("Add and remove a coin", () => {
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
    cy.o("coins/bitfinex/USD/BTC/exchange").contains("bitfinex")
    cy.o("coins/bitfinex/USD/BTC/name").contains("BTC/USD")
    cy.o("coins/bitfinex/USD/BTC/price").contains(/^[0-9\.]*/)
    cy.o("addCoinModal").should("not.exist")
    cy.o("errorModal").should("not.exist")
    cy.o("coins/bitfinex/USD/BTC/remove").click()
    cy.o("coins/bitfinex/USD/BTC/exchange").should("not.exist")
  })
})
