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
import LimitOrder from "../components/LimitOrder"
import { isValidNumber } from "modules/common/util/numberUtils"
import { getSelectedCoin } from "../selectors/coins"
import exchangeService from "modules/market/exchangesService"
import * as errorActions from "../store/error/actions"
import { withAuth } from "modules/auth"
import { withLog } from "modules/log/"
import exchangesService from "modules/market/exchangesService"
import { withSocket } from "modules/socket/"
import { withFramework } from "FrameworkContainer"

class LimitOrderContainer extends React.Component {
  constructor(props) {
    super(props)
    this.state = {
      order: Immutable({
        limitPrice: "",
        amount: ""
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

  createOrder = direction => ({
    type: direction === "BUY" ? "BID" : "ASK",
    counter: this.props.coin.counter,
    base: this.props.coin.base,
    amount: this.state.order.amount,
    limitPrice: this.state.order.limitPrice
  })

  calculateOrder = async direction => {
    try {
      const response = await exchangeService.calculateOrder(
        this.props.coin.exchange,
        this.createOrder(direction)
      )

      if (!response.ok) {
        var errorMessage = null
        try {
          errorMessage = (await response.json()).message
        } catch (err) {
          // No-op
        }
        if (!errorMessage) {
          errorMessage = response.statusText ? response.statusText : "Server error (" + response.status + ")"
        }

        throw new Error(errorMessage)
      } else {
        const result = await response.json()
        console.log(result)
        this.setState(prev => ({
          order: {
            ...prev.order,
            amount: result.amount
          }
        }))
      }
    } catch (error) {
      this.props.dispatch(errorActions.setForeground(errorMessage))
    }
  }

  onSubmit = direction => {
    const order = this.createOrder(direction)
    this.props.socketApi.createPlaceholder(order)
    this.props.auth
      .authenticatedRequest(() => exchangesService.submitOrder(this.props.coin.exchange, order))
      .catch(error => {
        this.props.socketApi.removePlaceholder()
        this.props.logApi.errorPopup("Could not submit order: " + error.message)
      })
  }

  render() {
    const limitPriceValid =
      this.state.order.limitPrice &&
      isValidNumber(this.state.order.limitPrice) &&
      this.state.order.limitPrice > 0
    const amountValid =
      this.state.order.amount && isValidNumber(this.state.order.amount) && this.state.order.amount > 0

    return (
      <LimitOrder
        order={this.state.order}
        onChange={this.onChange}
        onFocus={this.onFocus}
        onBuy={() => this.onSubmit("BUY")}
        onSell={() => this.onSubmit("SELL")}
        onSetMaxAmount={this.calculateOrder}
        limitPriceValid={limitPriceValid}
        amountValid={amountValid}
        coin={this.props.coin}
      />
    )
  }
}

function mapStateToProps(state) {
  return {
    coin: getSelectedCoin(state)
  }
}

export default withFramework(withSocket(withLog(withAuth(connect(mapStateToProps)(LimitOrderContainer)))))
