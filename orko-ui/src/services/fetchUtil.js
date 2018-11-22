import Cookies from "cookies-js"

const defaultSettings = { method: "GET", mode: "cors", redirect: "follow" }

const ACCESS_TOKEN = "accessToken"
const X_XSRF_TOKEN = "x-xsrf-token"

var xsrfToken = localStorage.getItem(X_XSRF_TOKEN)

export function setAccessToken(token, expires, httpsOnly) {
  Cookies.set(ACCESS_TOKEN, token, {
    path: "/",
    expires,
    secure: window.location.protocol === "https:"
  })
}

export function setXsrfToken(token) {
  xsrfToken = token
  localStorage.setItem(X_XSRF_TOKEN, token)
}

export function clearXsrfToken() {
  xsrfToken = undefined
  localStorage.removeItem(X_XSRF_TOKEN)
}

export function get(url) {
  return fetch(new Request("/api/" + url, action("GET")))
}

export function put(url, content) {
  return fetch(new Request("/api/" + url, action("PUT", content)))
}

export function post(url, content) {
  return fetch(new Request("/api/" + url, action("POST", content)))
}

export function del(url, content) {
  return fetch(new Request("/api/" + url, action("DELETE", content)))
}

function action(method, content) {
  return {
    ...defaultSettings,
    body: content,
    method: method,
    headers: xsrfToken
      ? new Headers({
          [X_XSRF_TOKEN]: xsrfToken,
          "Content-type": "application/json"
        })
      : new Headers({
          "Content-type": "application/json"
        })
  }
}
