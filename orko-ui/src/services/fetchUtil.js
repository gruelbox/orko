import Cookies from "cookies-js"

const defaultSettings = { method: "GET", mode: "cors", redirect: "follow" }

const ACCESS_TOKEN = "accessToken"
const X_XSRF_TOKEN = "X-XSRF-TOKEN"

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
    headers: new Headers({
      [X_XSRF_TOKEN]: Cookies.get(ACCESS_TOKEN),
      "Content-type": "application/json"
    })
  }
}
