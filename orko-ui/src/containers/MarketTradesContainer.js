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
import TradeHistory from "../components/TradeHistory"
import Loading from "../components/primitives/Loading"
import { getMarketTradeHistory, getSelectedCoin } from "../selectors/coins"

class MarketTradesContainer extends React.Component {
  render() {
    return !this.props.tradeHistory ? (
      <Loading p={2} />
    ) : (
      <TradeHistory
        coin={this.props.coin}
        trades={this.props.tradeHistory}
        excludeFees={true}
      />
    )
  }
}

function mapStateToProps(state, props) {
  return {
    tradeHistory: getMarketTradeHistory(state),
    coin: getSelectedCoin(state)
  }
}

export default connect(mapStateToProps)(MarketTradesContainer)
