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

  async whitelist(token) {
    const response = await put("auth?token=" + token)
    if (!response.ok) {
      throw new Error("Whitelisting failed (" + response.statusText + ")")
    }
    return true
  }

  async clearWhiteList() {
    const response = await del("auth")
    if (!response.ok) {
      throw new Error(
        "Failed to clear whitelisting (" + response.statusText + ")"
      )
    }
  }

  async simpleLogin(credentials) {
    const response = await post("auth/login", null, JSON.stringify(credentials))
    if (!response.ok) {
      throw new Error("Login failed (" + response.statusText + ")")
    }
    const received = await response.json()
    if (!received.success) {
      throw new Error("Login failed")
    }
    return received.token
  }

  async config() {
    return await get("auth/config")
  }
}

export default new AuthService()
