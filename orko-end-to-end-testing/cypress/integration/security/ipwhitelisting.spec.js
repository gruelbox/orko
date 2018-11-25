import crypto from "crypto"
import authenticator from "otplib/authenticator"
import { APP_URL } from "../../util/constants"

authenticator.options = {
  crypto
}

const IP_WHITELISTING_SECRET_INVALID = "KJY4B3ZOFWRNNCN3"
const IP_WHITELISTING_SECRET = "KJY4B3ZOFWRNNCN4"
const LOGIN_SECRET = "O546XLKJMJIQM3PW"
const LOGIN_USER = "ci"
const LOGIN_PW = "tester"

const NOT_WHITELISTED = 403
const NOT_AUTHENTICATED = 401

context("Check various APIs not accessible without IP whitelisting", () => {
  clearWhitelisting()

  checkResponseCode("/api/exchanges", NOT_WHITELISTED)
  checkResponseCode("/api/exchanges/binance/orders", NOT_WHITELISTED)

  it("PUT /api/auth?token=INVALID", () => {
    cy.request({
      method: "PUT",
      url:
        APP_URL + "/api/auth?token=" + tokenFor(IP_WHITELISTING_SECRET_INVALID),
      failOnStatusCode: false
    }).should(response => {
      expect(response.status).to.eq(403)
    })
  })

  it("PUT /api/auth?token=VALID", () => {
    cy.request({
      method: "PUT",
      url: APP_URL + "/api/auth?token=" + tokenFor(IP_WHITELISTING_SECRET)
    }).should(response => {
      expect(response.body).to.eq("Whitelisting successful")
    })
  })

  checkResponseCode("/api/exchanges", NOT_AUTHENTICATED)
  checkResponseCode("/api/exchanges/binance/orders", NOT_AUTHENTICATED)

  it("DELETE /api/auth", () => {
    cy.request({
      method: "DELETE",
      url: APP_URL + "/api/auth"
    })
  })

  checkResponseCode("/api/exchanges", NOT_WHITELISTED)
})

function clearWhitelisting() {
  it("Clear authentication", () => {
    cy.request({
      method: "DELETE",
      url: APP_URL + "/api/auth",
      failOnStatusCode: false
    })
  })
}

function tokenFor(secret) {
  return authenticator.generate(secret)
}

function checkResponseCode(endpoint, code) {
  it("GET " + endpoint + " (unauthenticated)", () => {
    cy.request({
      url: APP_URL + endpoint,
      failOnStatusCode: false
    }).should(response => {
      expect(response.status).to.eq(code)
    })
  })
}
