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

context("New version", () => {
  beforeEach(function() {
    // Unload the site so that XHR requests overlapping setup don't
    // log the app back out again
    cy.visit("/empty.html")
    // Now start the login process
    cy.whitelist()
  })

  it("No new versions", () => {
    cy.server()
    cy.route("get", "/api/support/meta", { version: "0.1.0" })
    cy.route("get", "https://api.github.com/repos/gruelbox/orko/releases", [
      { tag_name: "0.0.2", body: "TEST 0.0.2" },
      { tag_name: "0.1.0", body: "TEST 0.1.0" }
    ]).as("getReleases")
    cy.loginApi()
    cy.visit("/")
    cy.wait("@getReleases", {
      requestTimeout: LONG_WAIT
    })
    cy.wait(2000)
    cy.o("newReleases").should("not.exist")
  })

  it("One new version, test ignore/later", () => {
    cy.server()
    cy.route("get", "/api/support/meta", { version: "0.0.9" })
    cy.route("get", "https://api.github.com/repos/gruelbox/orko/releases", [
      { tag_name: "0.0.2", body: "TEST 0.0.2" },
      { tag_name: "0.1.0", body: "TEST 0.1.0" }
    ]).as("getReleases")
    cy.loginApi()
    cy.visit("/")
    cy.wait("@getReleases", {
      requestTimeout: LONG_WAIT
    })
    cy.wait(2000)
    cy.o("newReleases").within(() => {
      cy.o("release/0.1.0").should("exist")
      cy.o("release/0.0.2").should("not.exist")
      cy.o("later").click()
    })
    cy.o("newReleases").should("not.exist")
    cy.reload()
    cy.o("newReleases").within(() => {
      cy.o("release/0.1.0").should("exist")
      cy.o("release/0.0.2").should("not.exist")
      cy.o("ignore").click()
    })
    cy.o("newReleases").should("not.exist")
    cy.reload()
    cy.o("newReleases").should("not.exist")
  })

  it("Two new versions", () => {
    cy.server()
    cy.route("get", "/api/support/meta", { version: "0.0.1" })
    cy.route("get", "https://api.github.com/repos/gruelbox/orko/releases", [
      { tag_name: "0.0.2", body: "TEST 0.0.2" },
      { tag_name: "0.1.0", body: "TEST 0.1.0" }
    ]).as("getReleases")
    cy.loginApi()
    cy.visit("/")
    cy.wait("@getReleases", {
      requestTimeout: LONG_WAIT
    })
    cy.wait(2000)
    cy.o("newReleases").within(() => {
      cy.o("release/0.1.0").should("exist")
      cy.o("release/0.0.2").should("exist")
    })
  })
})
