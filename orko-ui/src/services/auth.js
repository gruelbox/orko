import { post, put, get, del } from "./fetchUtil"

class AuthService {
  async checkWhiteList() {
    const response = await get("auth")
    if (!response.ok) {
      throw Error(response.statusText)
    }
    const result = await response.text()
    return result === "true"
  }

  async checkLoggedIn() {
    const response = await get("exchanges")
    return response.ok
  }

  async whitelist(token) {
    const response = await put("auth?token=" + token)
    if (!response.ok) {
      var message
      try {
        message = response.text()
      } catch (error) {
        message = response.statusText
      }
      throw new Error("Whitelisting failed (" + message + ")")
    }
    return true
  }

  async clearWhiteList() {
    const response = await del("auth")
    if (!response.ok) {
      throw new Error(
        "Failed to clear whitelisting (" + (await response.text()) + ")"
      )
    }
  }

  async simpleLogin(credentials) {
    const response = await post("auth/login", JSON.stringify(credentials))
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
    const result = await get("auth/config")
    return await result.json()
  }
}

export default new AuthService()
