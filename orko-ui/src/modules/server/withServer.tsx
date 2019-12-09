import React from "react"
import { ServerContext } from "./ServerContext"
export function withServer(WrappedComponent: React.FC | React.ComponentClass) {
  return (props: any) => (
    <ServerContext.Consumer>
      {serverApi => <WrappedComponent {...props} serverApi={serverApi}></WrappedComponent>}
    </ServerContext.Consumer>
  )
}
