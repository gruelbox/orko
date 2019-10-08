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
import { post, put, get, del } from "modules/common/util/fetchUtil"

class AuthService {
  async checkWhiteList(): Promise<boolean> {
    const response: any = await get("auth")
    if (!response.ok) {
      throw Error(response.statusText)
    }
    const result = await response.text()
    return result === "true"
  }

  async checkLoggedIn(): Promise<boolean> {
    const response: any = await get("exchanges")
    return response.ok
  }

  async whitelist(token: string) {
    const response: any = await put("auth?token=" + token)
    if (!response.ok) {
      if (response.status === 404)
        throw new Error("Whitelisting failed (invalid token)")
      var message
      try {
        message = await response.text()
      } catch (error) {
        message = response.statusText
      }
      throw new Error("Whitelisting failed (" + message + ")")
    }
    return true
  }

  async clearWhiteList() {
    const response: any = await del("auth")
    if (!response.ok) {
      throw new Error(
        "Failed to clear whitelisting (" + (await response.text()) + ")"
      )
    }
  }

  async simpleLogin(credentials) {
    const response: any = await post("auth/login", JSON.stringify(credentials))
    if (!response.ok) {
      throw new Error("Login failed (" + response.statusText + ")")
    }
    const received = await response.json()
    if (!received.success) {
      throw new Error("Login failed")
    }
    return received
  }

  async config() {
    const result: any = await get("auth/config")
    return await result.json()
  }
}

export default new AuthService()
