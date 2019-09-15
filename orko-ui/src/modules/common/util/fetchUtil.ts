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

// Uses polyfilled fetch since this is XHR backed and thus will work with
// Cypress stubbing. Browser native fetch is not supported currently:
// https://github.com/cypress-io/cypress/issues/687
import { fetch as fetchPolyfill } from "whatwg-fetch"

const defaultSettings = { method: "GET", mode: "cors", redirect: "follow" }

const ACCESS_TOKEN = "accessToken"
const X_XSRF_TOKEN = "x-xsrf-token"

let xsrfToken = localStorage.getItem(X_XSRF_TOKEN)

export function setAccessToken(token: string, expires: boolean): void {
  Cookies.set(ACCESS_TOKEN, token, {
    expires,
    path: "/",
    secure: window.location.protocol === "https:"
  })
}

export function setXsrfToken(token: string): void {
  xsrfToken = token
  localStorage.setItem(X_XSRF_TOKEN, token)
}

export function clearXsrfToken(): void {
  xsrfToken = undefined
  localStorage.removeItem(X_XSRF_TOKEN)
}

export async function getWeb(url: string): Promise<object> {
  return fetchPolyfill(url)
}

export async function get(url: string): Promise<object> {
  return fetchPolyfill("/api/" + url, action("GET"))
}

export async function put(url: string, content?: string): Promise<object> {
  return fetchPolyfill("/api/" + url, action("PUT", content))
}

export async function post(url: string, content?: string): Promise<object> {
  return fetchPolyfill("/api/" + url, action("POST", content))
}

export async function del(url: string, content?: string): Promise<object> {
  return fetchPolyfill("/api/" + url, action("DELETE", content))
}

function action(method: string, content?: string): object {
  return {
    ...defaultSettings,
    body: content,
    headers: xsrfToken
      ? {
          [X_XSRF_TOKEN]: xsrfToken,
          "Content-type": "application/json"
        }
      : {
          "Content-type": "application/json"
        },
    method
  }
}
