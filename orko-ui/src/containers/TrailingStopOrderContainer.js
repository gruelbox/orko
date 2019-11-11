/*
 * Orko
 * Copyright © 2018-2019 Graham Crockford
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
import React from "react"
import { connect } from "react-redux"
import Immutable from "seamless-immutable"
import uuidv4 from "uuid/v4"
import TrailingStopOrder from "../components/TrailingStopOrder"
import { isValidNumber } from "modules/common/util/numberUtils"
import { getSelectedCoin } from "../selectors/coins"
import { withSocket } from "modules/socket/"
import { withFramework } from "FrameworkContainer"
import { JobType, withServer, TradeDirection } from "modules/server"

class TrailingStopOrderContainer extends React.Component {
  constructor(props) {
    super(props)
    this.state = {
      order: Immutable({
        stopPrice: "",
        limitPrice: "",
        amount: "",
        useExchange: false
      })
    }
  }

  onChange = order => {
    this.setState({
      order
    })
  }

  onFocus = focusedProperty => {
    this.props.frameworkApi.setLastFocusedFieldPopulater(value => {
      this.setState(prev => ({
        order: prev.order.merge({
          [focusedProperty]: value
        })
      }))
    })
  }

  currentPrice = direction =>
    direction === TradeDirection.BUY
      ? this.props.socketApi.selectedCoinTicker.ask
      : this.props.socketApi.selectedCoinTicker.bid

  createJob = direction => {
    const startPrice = this.currentPrice(direction)
    return {
      jobType: JobType.SOFT_TRAILING_STOP,
      id: uuidv4(),
      tickTrigger: {
        exchange: this.props.coin.exchange,
        counter: this.props.coin.counter,
        base: this.props.coin.base
      },
      direction,
      amount: this.state.order.amount,
      startPrice,
      lastSyncPrice: startPrice,
      stopPrice: this.state.order.stopPrice,
      limitPrice: this.state.order.limitPrice
    }
  }

  onSubmit = async direction => {
    this.props.serverApi.submitJob(this.createJob(direction))
  }

  render() {
    const stopPriceValid =
      this.state.order.stopPrice &&
      isValidNumber(this.state.order.stopPrice) &&
      this.state.order.stopPrice > 0
    const limitPriceValid =
      this.state.order.limitPrice &&
      isValidNumber(this.state.order.limitPrice) &&
      this.state.order.limitPrice > 0
    const amountValid =
      this.state.order.amount && isValidNumber(this.state.order.amount) && this.state.order.amount > 0

    return (
      <TrailingStopOrder
        order={this.state.order}
        onChange={this.onChange}
        onFocus={this.onFocus}
        onBuy={() => this.onSubmit(TradeDirection.BUY)}
        onSell={() => this.onSubmit(TradeDirection.SELL)}
        stopPriceValid={stopPriceValid}
        limitPriceValid={limitPriceValid}
        amountValid={amountValid}
        coin={this.props.coin}
        currentPrice={this.currentPrice}
        tickerAvailable={!!this.props.socketApi.selectedCoinTicker}
      />
    )
  }
}

function mapStateToProps(state) {
  return {
    coin: getSelectedCoin(state)
  }
}

export default withServer(withFramework(withSocket(connect(mapStateToProps)(TrailingStopOrderContainer))))
