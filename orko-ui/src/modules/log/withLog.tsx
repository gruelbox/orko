import React from "react"
import { LogContext } from "./LogContext"
export function withLog(WrappedComponent: React.FC | React.ComponentClass) {
  return (props: any) => (
    <LogContext.Consumer>
      {logApi => <WrappedComponent {...props} logApi={logApi}></WrappedComponent>}
    </LogContext.Consumer>
  )
}
