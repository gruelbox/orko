import React from "react"

export interface AuthApi {
  authorised: boolean
  logout(): void
  clearWhitelisting(): void
  wrappedRequest(apiRequest, jsonHandler, errorHandler, onSuccess?)
}

export const AuthContext: React.Context<AuthApi> = React.createContext({
  authorised: Boolean(false),
  logout: () => {},
  clearWhitelisting: () => {},
  wrappedRequest: (apiRequest, jsonHandler, errorHandler, onSuccess?) => {}
})
