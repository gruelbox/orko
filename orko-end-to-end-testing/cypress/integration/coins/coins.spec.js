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

  it("Add a coin", () => {
    cy.get("[data-orko=addCoin]").click()
    cy.get("[data-orko=addCoinModal]").within(() => {
      cy.get("[data-orko=selectExchange]").click()
      cy.get("[class='visible menu transition']")
        .contains("gdax")
        .click()
      cy.get("[data-orko=selectPair]").click()
      cy.get("[class='visible menu transition']")
        .contains("BTC/USD")
        .click()
      cy.get("[data-orko=addCoinSubmit]").click()
    })
    cy.get("[data-orko=addCoinModal]").should("not.exist")
    cy.get("[data-orko=errorModal]").should("not.exist")
  })
})
