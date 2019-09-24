import React from "react"
import { SocketContext } from "./SocketContext"
export function withSocket(WrappedComponent: React.FC | React.ComponentClass) {
  return (props: any) => (
    <SocketContext.Consumer>
      {socketApi => <WrappedComponent {...props} socketApi={socketApi}></WrappedComponent>}
    </SocketContext.Consumer>
  )
}
