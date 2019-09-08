import React from "react"
import { AuthContext } from "./AuthContext"
export function withAuth(WrappedComponent: React.FC | React.ComponentClass) {
  return (props: any) => (
    <AuthContext.Consumer>
      {auth => <WrappedComponent {...props} auth={auth}></WrappedComponent>}
    </AuthContext.Consumer>
  )
}
