import React from "react"
import { Switch, Route } from "react-router-dom"
import { coin as createCoin } from "./store/coin/reducer"

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

export default class WithCoinParameter extends React.Component {
  render() {
    const Provider = props => (
      <ExtractCoin {...props} component={this.props.component} />
    )
    return (
      <Switch>
        <Route
          path="/coin/:exchange/:counter/:base"
          component={Provider}
        />
      </Switch>
    )
  }
}