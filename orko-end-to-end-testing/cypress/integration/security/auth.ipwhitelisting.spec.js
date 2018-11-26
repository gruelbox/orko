import crypto from "crypto"
import authenticator from "otplib/authenticator"
import {
  APP_URL,
  NOT_WHITELISTED,
  NOT_AUTHENTICATED
} from "../../util/constants"
import { whitelist, clearWhitelisting, tokenForSecret } from "../../util/login"

const IP_WHITELISTING_SECRET_INVALID = "KJY4B3ZOFWRNNCN3"

context("Check various APIs not accessible without IP whitelisting", () => {
  clearWhitelisting()

  checkResponseCode("/api/exchanges", NOT_WHITELISTED)
  checkResponseCode("/api/exchanges/binance/orders", NOT_WHITELISTED)

  it("PUT /api/auth?token=INVALID", () => {
    cy.request({
      method: "PUT",
      url:
        APP_URL +
        "/api/auth?token=" +
        tokenForSecret(IP_WHITELISTING_SECRET_INVALID),
      failOnStatusCode: false
    }).should(response => {
      expect(response.status).to.eq(403)
    })
  })

  whitelist()

  checkResponseCode("/api/exchanges", NOT_AUTHENTICATED)
  checkResponseCode("/api/exchanges/binance/orders", NOT_AUTHENTICATED)

  clearWhitelisting()

  checkResponseCode("/api/exchanges", NOT_WHITELISTED)
})

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
