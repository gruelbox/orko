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
import StopOrder from "../components/StopOrder"
import { isValidNumber } from "modules/common/util/numberUtils"
import { getSelectedCoin } from "../selectors/coins"
import uuidv4 from "uuid/v4"
import exchangesService from "modules/market/exchangesService"
import { withSocket } from "modules/socket/"
import { withFramework } from "FrameworkContainer"
import { JobType, withServer } from "modules/server"
import { withAuth } from "modules/auth"
import { withLog } from "modules/log"

function coinServerSideSupported(coin) {
  return !["bittrex"].includes(coin.exchange)
}

function coinAllowsLimitStops(coin, useExchange) {
  return !useExchange || !["bitfinex", "bitmex"].includes(coin.exchange)
}

function coinAllowsMarketStops(coin, useExchange) {
  return useExchange && coin.exchange !== "binance"
}

function coinAllowsBuyStops(coin, useExchange) {
  return !useExchange || coin.exchange !== "binance"
}

class StopOrderContainer extends React.Component {
  constructor(props) {
    super(props)
    this.state = {
      order: Immutable({
        stopPrice: "",
        limitPrice: "",
        amount: "",
        useExchange: coinServerSideSupported(props.coin)
      })
    }
  }

  onChange = order => {
    this.setState({
      order: Immutable({
        ...order,
        // Clear the limit price if it's not supported anymore
        limitPrice: coinAllowsLimitStops(this.props.coin, order.useExchange) ? order.limitPrice : ""
      })
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

  createOrder = direction => ({
    type: direction === "BUY" ? "BID" : "ASK",
    counter: this.props.coin.counter,
    base: this.props.coin.base,
    amount: this.state.order.amount,
    stopPrice: this.state.order.stopPrice,
    limitPrice: this.state.order.limitPrice
  })

  createJob = direction => ({
    jobType: JobType.OCO,
    id: uuidv4(),
    tickTrigger: {
      exchange: this.props.coin.exchange,
      counter: this.props.coin.counter,
      base: this.props.coin.base
    },
    [direction === "BUY" ? "high" : "low"]: {
      thresholdAsString: this.state.order.stopPrice,
      job: {
        jobType: JobType.LIMIT_ORDER,
        id: uuidv4(),
        direction: direction,
        tickTrigger: {
          exchange: this.props.coin.exchange,
          counter: this.props.coin.counter,
          base: this.props.coin.base
        },
        amount: this.state.order.amount,
        limitPrice: this.state.order.limitPrice
      }
    }
  })

  onSubmit = direction => {
    if (this.state.order.useExchange) {
      const order = this.createOrder(direction)
      this.props.socketApi.createPlaceholder(order)
      this.props.auth
        .authenticatedRequest(() => exchangesService.submitOrder(this.props.coin.exchange, order))
        .catch(error => {
          this.props.socketApi.removePlaceholder()
          this.props.logApi.errorPopup("Could not submit order: " + error.message)
        })
    } else {
      this.props.serverApi.submitJob(this.createJob(direction))
    }
  }

  render() {
    const stopPriceValid =
      this.state.order.stopPrice &&
      isValidNumber(this.state.order.stopPrice) &&
      this.state.order.stopPrice > 0
    const blankLimitPrice = this.state.order.limitPrice === ""
    const validLimitPrice = isValidNumber(this.state.order.limitPrice) && this.state.order.limitPrice > 0
    const limitPriceValid =
      (validLimitPrice && coinAllowsLimitStops) || (blankLimitPrice && coinAllowsMarketStops)

    const amountValid =
      this.state.order.amount && isValidNumber(this.state.order.amount) && this.state.order.amount > 0

    return (
      <StopOrder
        order={this.state.order}
        onChange={this.onChange}
        onFocus={this.onFocus}
        onBuy={
          coinAllowsBuyStops(this.props.coin, this.state.order.useExchange)
            ? () => this.onSubmit("BUY")
            : null
        }
        onSell={() => this.onSubmit("SELL")}
        stopPriceValid={stopPriceValid}
        limitPriceValid={limitPriceValid}
        amountValid={amountValid}
        coin={this.props.coin}
        allowLimit={coinAllowsLimitStops(this.props.coin, this.state.order.useExchange)}
        allowMarket={coinAllowsMarketStops(this.props.coin, this.state.order.useExchange)}
        allowServerSide={coinServerSideSupported(this.props.coin, this.state.order.useExchange)}
      />
    )
  }
}

function mapStateToProps(state) {
  return {
    coin: getSelectedCoin(state)
  }
}

export default withServer(
  withServer(withFramework(withSocket(withAuth(withLog(connect(mapStateToProps)(StopOrderContainer))))))
)
