import React from "react"

export interface SocketApi {
  connected: boolean
  resubscribe(): void
}

export const SocketContext = React.createContext<SocketApi>({
  connected: false,
  resubscribe: () => {}
})
