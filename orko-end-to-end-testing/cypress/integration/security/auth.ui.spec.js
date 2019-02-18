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
  IP_WHITELISTING_SECRET,
  IP_WHITELISTING_SECRET_INVALID
} from "../../util/constants"
import { tokenForSecret } from "../../util/token"

context("Auth UI", () => {
  beforeEach(function() {
    cy.logout()
    cy.clearWhitelist()
  })

  it("Successful full login", () => {
    cy.visit("/")
    cy.o("whitelistingModal").within(() => {
      cy.o("token").type(tokenForSecret(IP_WHITELISTING_SECRET))
      cy.o("whitelistingSubmit").click()
    })
    cy.login({ visit: false })
    cy.getCookie("accessToken")
      .its("httpOnly")
      .should("eq", true)
  })

  it("Invalid whitelisting attempt", () => {
    cy.visit("/")
    cy.o("whitelistingModal").within(() => {
      cy.o("token").type(tokenForSecret(IP_WHITELISTING_SECRET_INVALID))
      cy.o("whitelistingSubmit").click()
      cy.contains("Error")
      cy.contains("Whitelisting failed")
    })
    cy.requestNoFail("/api/exchanges")
      .its("status")
      .should("eq", 403)
  })

  it("Wrong user", () => {
    cy.whitelist()
    cy.visit("/")
    cy.login({ validUser: false })
  })

  it("Wrong password", () => {
    cy.whitelist()
    cy.visit("/")
    cy.login({ validPassword: false })
  })

  it("Wrong token", () => {
    cy.whitelist()
    cy.visit("/")
    cy.login({ validToken: false })
  })

  it("Blank token", () => {
    cy.whitelist()
    cy.visit("/")
    cy.login({ hasToken: false })
  })
})
