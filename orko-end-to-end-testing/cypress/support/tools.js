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

export function addSubscription(ticker) {
  return cy.secureRequest({
    method: "PUT",
    url: "/api/subscriptions",
    body: ticker
  })
}
