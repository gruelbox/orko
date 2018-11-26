import {
  IP_WHITELISTING_SECRET,
  IP_WHITELISTING_SECRET_INVALID,
  LOGIN_USER,
  LOGIN_PW,
  LOGIN_SECRET,
  LOGIN_SECRET_INVALID
} from "../../util/constants"
import { tokenForSecret } from "../../util/token"

context("Auth UI", () => {
  beforeEach(function() {
    cy.logout()
    cy.clearWhitelist()
  })

  it("Successful full login", () => {
    cy.visit("/")

    cy.requestNoFail("/api/exchanges")
      .its("status")
      .should("eq", 403)

    cy.get("[data-orko=token]").type(tokenForSecret(IP_WHITELISTING_SECRET))
    cy.get("[data-orko=submitModal]").click()

    cy.login({ visit: false })
  })

  it("Invalid whitelisting attempt", () => {
    cy.visit("/")

    cy.requestNoFail("/api/exchanges")
      .its("status")
      .should("eq", 403)

    cy.get("[data-orko=token]").type(
      tokenForSecret(IP_WHITELISTING_SECRET_INVALID)
    )
    cy.get("[data-orko=submitModal]").click()

    cy.get("div").contains("Error")
    cy.get("p").contains("Whitelisting failed")

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
