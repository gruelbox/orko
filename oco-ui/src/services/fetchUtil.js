import ReconnectingWebSocket from "reconnecting-websocket"
import runtimeEnv from "@mars/heroku-js-runtime-env"

const defaultSettings = { method: "GET", mode: "cors", redirect: "follow" }

export function get(url, token) {
  return fetch(new Request("/api/" + url, action("GET", token)))
}

export function put(url, token, content) {
  return fetch(new Request("/api/" + url, action("PUT", token, content)))
}

export function del(url, token) {
  return fetch(new Request("/api/" + url, action("DELETE", token)))
}

export function ws(url, token) {
  const env = runtimeEnv()
  var fullUrl
  if (env.REACT_APP_WS_URL) {
    fullUrl = env.REACT_APP_WS_URL + "/" + url
  } else {
    const protocol = window.location.protocol === "https:" ? "wss:" : "ws:"
    fullUrl = protocol + "//" + window.location.host + "/" + url
  }
  if (token) {
    return new ReconnectingWebSocket(fullUrl, ["auth", token])
  } else {
    return new ReconnectingWebSocket(fullUrl)
  }
}

function action(method, token, content) {
  if (token)
    return {
      ...defaultSettings,
      body: content,
      method: method,
      credentials: "include",
      headers: new Headers({
        Authorization: "Bearer " + token,
        "Content-type": "application/json"
      })
    }
  else
    return {
      ...defaultSettings,
      method: method
    }
}
