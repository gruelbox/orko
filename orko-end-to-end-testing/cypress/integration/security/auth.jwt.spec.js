import {
  APP_URL,
  LOGIN_USER,
  LOGIN_PW,
  LOGIN_SECRET,
  NOT_AUTHENTICATED
} from "../../util/constants"
import { whitelist, clearWhitelisting, tokenForSecret } from "../../util/login"

export const LOGIN_SECRET_INVALID = "O546XLKJMJIQM3PX"

context("Check the login sequence for JWT authentication", () => {
  clearWhitelisting()
  whitelist()

  it("Check we're seeing the API as unauthenticated", () => {
    checkResponseCode("/api/exchanges", NOT_AUTHENTICATED)
    checkResponseCode("/api/exchanges/binance/orders", NOT_AUTHENTICATED)
  })

  it("Wrong username", () => {
    cy.request({
      method: "POST",
      url: APP_URL + "/api/auth/login",
      failOnStatusCode: false,
      body: {
        username: LOGIN_USER + "x",
        password: LOGIN_PW,
        secondFactor: tokenForSecret(LOGIN_SECRET)
      }
    }).should(response => {
      expect(response.status).to.eq(403)
    })
  })

  it("Wrong password", () => {
    cy.request({
      method: "POST",
      url: APP_URL + "/api/auth/login",
      failOnStatusCode: false,
      body: {
        username: LOGIN_USER,
        password: LOGIN_PW + "x",
        secondFactor: tokenForSecret(LOGIN_SECRET)
      }
    }).should(response => {
      expect(response.status).to.eq(403)
    })
  })

  it("Wrong token", () => {
    cy.request({
      method: "POST",
      url: APP_URL + "/api/auth/login",
      failOnStatusCode: false,
      body: {
        username: LOGIN_USER,
        password: LOGIN_PW,
        secondFactor: tokenForSecret(LOGIN_SECRET_INVALID)
      }
    }).should(response => {
      expect(response.status).to.eq(403)
    })
  })

  it("Successful login but incorrect use of XSRF", () => {
    cy.request({
      method: "POST",
      url: APP_URL + "/api/auth/login",
      failOnStatusCode: false,
      body: {
        username: LOGIN_USER,
        password: LOGIN_PW,
        secondFactor: tokenForSecret(LOGIN_SECRET)
      }
    }).should(response => {
      expect(response.body).to.have.property("xsrf")
      checkResponseCode("/api/exchanges", 401, { xsrfToken: "ISWRONG" })
    })
  })

  it("Successful login and XSRF", () => {
    cy.visit(APP_URL)
    cy.request({
      method: "POST",
      url: APP_URL + "/api/auth/login",
      body: {
        username: LOGIN_USER,
        password: LOGIN_PW,
        secondFactor: tokenForSecret(LOGIN_SECRET)
      }
    }).then(response => {
      expect(response.body).to.have.property("xsrf")
      const xsrfToken = response.body.xsrf
      checkResponseCode("/api/exchanges", 200, { xsrfToken })
      checkResponseCode("/api/exchanges/binance/orders", 200, { xsrfToken })
    })
  })

  clearWhitelisting()
})

function checkResponseCode(endpoint, code, options) {
  var args = {
    url: APP_URL + endpoint,
    failOnStatusCode: false
  }

  if (options && options.xsrfToken) {
    args.headers = {
      ["x-xsrf-token"]: options.xsrfToken
    }
  }

  cy.request(args).should(response => {
    expect(response.status).to.eq(code)
  })
}
