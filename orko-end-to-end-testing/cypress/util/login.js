import crypto from "crypto"
import authenticator from "otplib/authenticator"
import {
  APP_URL,
  IP_WHITELISTING_SECRET,
  LOGIN_SECRET,
  LOGIN_USER,
  LOGIN_PW,
  NOT_WHITELISTED,
  NOT_AUTHENTICATED
} from "./constants"

authenticator.options = {
  crypto
}

export function clearWhitelisting() {
  it("Clear authentication", () => {
    cy.request({
      method: "DELETE",
      url: APP_URL + "/api/auth",
      failOnStatusCode: false
    })
  })
}

export function whitelist() {
  it("PUT /api/auth?token=VALID", () => {
    cy.request({
      method: "PUT",
      url: APP_URL + "/api/auth?token=" + tokenForSecret(IP_WHITELISTING_SECRET)
    }).should(response => {
      expect(response.body).to.eq("Whitelisting successful")
    })
  })
}

export function tokenForSecret(secret) {
  return authenticator.generate(secret)
}
