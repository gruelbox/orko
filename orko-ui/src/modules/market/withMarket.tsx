import React from "react"
import { MarketContext } from "./MarketContext"
export function withMarket(WrappedComponent: React.FC | React.ComponentClass) {
  return (props: any) => (
    <MarketContext.Consumer>
      {marketApi => <WrappedComponent {...props} marketApi={marketApi}></WrappedComponent>}
    </MarketContext.Consumer>
  )
}
