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

export function getWeb(url) {
  return fetch(new Request(url, action("GET")))
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
