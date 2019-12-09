import React from "react"

export enum AuthenticatedRequestResponseType {
  NONE,
  TEXT,
  JSON
}

export interface AuthenticatedRequestOptions {
  responseType?: AuthenticatedRequestResponseType
}

export interface AuthApi {
  authorised: boolean
  logout(): void
  clearWhitelisting(): void
  wrappedRequest(apiRequest, jsonHandler, errorHandler, onSuccess?)
  authenticatedRequest<T extends unknown>(
    responseGenerator: () => Promise<Response>,
    options?: AuthenticatedRequestOptions
  ): Promise<T>
}

export const AuthContext: React.Context<AuthApi> = React.createContext(null)
