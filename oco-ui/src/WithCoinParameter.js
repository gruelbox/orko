import React from "react"
import { Switch, Route } from "react-router-dom"
import { coin as createCoin } from "./util/coinUtils"

class ExtractCoin extends React.Component {
  render() {
    const coin = this.props.match.params.exchange
      ? createCoin(
          this.props.match.params.exchange,
          this.props.match.params.counter,
          this.props.match.params.base
        )
      : undefined
    const ChildComponent = this.props.component
    return <ChildComponent coin={coin} />
  }
}

const WithCoinParameter = props => (
  <Switch>
    <Route
      path="/coin/:exchange/:counter/:base"
      render={routeProps => <ExtractCoin {...routeProps} component={props.component} />}
    />
    <Route
      render={routeProps => <props.component />}
    />
  </Switch>
)

export default WithCoinParameter