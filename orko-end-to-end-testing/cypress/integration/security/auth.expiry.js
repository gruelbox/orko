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
import { LONG_WAIT } from "../../util/constants"

context("Authorisation expiry", () => {
  beforeEach(function() {
    // Unload the site so that XHR requests overlapping setup don't
    // log the app back out again
    cy.visit("/empty.html")
    cy.whitelist()
    cy.loginApi()
    cy.visit("/")
    cy.o("loginModal").should("not.exist")
  })

  it("Whitelist expiry", () => {
    cy.server()
    cy.wait(2000)
    cy.request({
      method: "DELETE",
      url: "/api/auth"
    })
    cy.route("get", "/api/jobs").as("getJobs")
    cy.wait("@getJobs", {
      requestTimeout: LONG_WAIT
    })
    cy.o("whitelistingModal").should("exist")
    cy.o("errorModal").should("not.exist")
  })
})
