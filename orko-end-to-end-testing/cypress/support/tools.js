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
export function clearSubscriptions() {
  return cy
    .secureRequest({
      url: "/api/subscriptions",
      method: "GET"
    })
    .should(response => {
      expect(response.status).to.eq(200)
      expect(response.body).to.be.an.array
      response.body.forEach(ticker => {
        cy.secureRequest({
          url: "/api/subscriptions",
          method: "DELETE",
          body: ticker
        })
      })
    })
}

export function clearJobs() {
  return cy.secureRequest({
    url: "/api/jobs",
    method: "DELETE"
  })
}

export function cancelOrder(tickerSpec, order) {
  cy.secureRequest({
    url:
      "/api/exchanges/" +
      tickerSpec.exchange +
      "/markets/" +
      tickerSpec.base +
      "-" +
      tickerSpec.counter +
      "/orders/" +
      order.id +
      "?orderType=" +
      order.type,
    method: "DELETE"
  })
}

export function listOrders(tickerSpec) {
  return cy
    .secureRequest({
      url:
        "/api/exchanges/" +
        tickerSpec.exchange +
        "/markets/" +
        tickerSpec.base +
        "-" +
        tickerSpec.counter +
        "/orders",
      method: "GET"
    })
    .should(response => {
      expect(response.status).to.eq(200)
      expect(response.body).to.be.an.object
    })
    .its("body")
}

export function clearOrders(tickerSpec) {
  return listOrders(tickerSpec).then(body => {
    body.openOrders.forEach(order => cancelOrder(tickerSpec, order))
  })
}

export function addSubscription(ticker) {
  return cy.secureRequest({
    method: "PUT",
    url: "/api/subscriptions",
    body: ticker
  })
}
