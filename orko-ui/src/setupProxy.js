const proxy = require("http-proxy-middleware")

module.exports = function(app) {
  app.use(proxy("/api", { target: "http://localhost:8080", xfwd: true }))
  app.use(proxy("/ws", { target: "ws://localhost:8080", ws: true, xfwd: true }))
}
