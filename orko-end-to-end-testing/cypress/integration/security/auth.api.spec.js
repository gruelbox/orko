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
context("Auth API", () => {
  beforeEach(function() {
    cy.logout()
  })

  it("Check whitelisting", () => {
    cy.clearWhitelist()

    cy.request("/api/auth")
      .its("body")
      .should("eq", "false")

    cy.requestNoFail("/api/exchanges")
      .its("status")
      .should("eq", 403)
    cy.requestNoFail("/api/exchanges/binance/orders")
      .its("status")
      .should("eq", 403)
    cy.requestNoFail("/admin")
      .its("status")
      .should("eq", 403)
    cy.requestNoFail("/api/db.zip")
      .its("status")
      .should("eq", 403)

    cy.whitelist({ valid: false })

    cy.requestNoFail("/api/exchanges")
      .its("status")
      .should("eq", 403)
    cy.requestNoFail("/api/exchanges/binance/orders")
      .its("status")
      .should("eq", 403)
    cy.requestNoFail("/admin")
      .its("status")
      .should("eq", 403)
    cy.requestNoFail("/api/db.zip")
      .its("status")
      .should("eq", 403)

    cy.whitelist()

    cy.request("/api/auth")
      .its("body")
      .should("eq", "true")

    cy.requestNoFail("/api/exchanges")
      .its("status")
      .should("eq", 401)
    cy.requestNoFail("/api/exchanges/binance/orders")
      .its("status")
      .should("eq", 401)
    cy.requestNoFail("/admin")
      .its("status")
      .should("eq", 401)
    cy.requestNoFail("/api/db.zip")
      .its("status")
      .should("eq", 401)

    cy.clearWhitelist({ failOnStatusCode: true })

    cy.requestNoFail("/api/exchanges")
      .its("status")
      .should("eq", 403)
  })

  it("Check failed logins", () => {
    cy.whitelist()
    cy.loginApi({ validUser: false })
    cy.loginApi({ validPassword: false })
    cy.loginApi({ validToken: false })
  })

  it("Check invalid XSRF", () => {
    cy.whitelist()
    cy.loginApi()
      .its("status")
      .should("eq", 200)
    cy.request({
      url: "/api/exchanges",
      failOnStatusCode: false,
      headers: {
        "x-xsrf-token": "WRONG"
      }
    })
      .its("status")
      .should("eq", 401)
    cy.getCookie("accessToken")
      .its("httpOnly")
      .should("eq", true)
  })

  it("Check success", () => {
    cy.whitelist()
    cy.loginApi()
      .its("body")
      .then(auth => {
        
        cy.request({
          url: "/api/exchanges",
          failOnStatusCode: false,
          headers: {
            "x-xsrf-token": auth.xsrf
          }
        })
          .its("status")
          .should("eq", 200)

        cy.request({
          url: "/admin",
          failOnStatusCode: false,
          headers: {
            "x-xsrf-token": auth.xsrf
          }
        })
          .its("status")
          .should("eq", 200)

        cy.request({
          url: "/api/db.zip"
        })
          .its("status")
          .should("eq", 200)
      })
    cy.getCookie("accessToken")
      .its("httpOnly")
      .should("eq", true)
  })
})
