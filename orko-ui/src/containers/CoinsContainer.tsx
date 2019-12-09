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
import React, { useContext, useMemo } from "react"
import { connect } from "react-redux"
import { Icon } from "semantic-ui-react"
import Section from "../components/primitives/Section"
import Link from "../components/primitives/Link"
import Coins from "../components/Coins"
import GetPageVisibility from "../components/GetPageVisibility"
import RenderIf from "../components/RenderIf"
import { MarketContext, Coin } from "modules/market"
import { SocketContext } from "modules/socket"
import { FrameworkContext } from "FrameworkContainer"
import { ServerContext, OcoJob } from "modules/server"
import { isAlert } from "util/jobUtils"

const buttons = () => (
  <Link to="/addCoin" data-orko="addCoin" title="Add a coin">
    <Icon name="add" />
  </Link>
)

interface CoinsContainerProps {
  referencePrices
}

const CoinsCointainer: React.FC<CoinsContainerProps> = ({ referencePrices }) => {
  const marketApi = useContext(MarketContext)
  const socketApi = useContext(SocketContext)
  const frameworkApi = useContext(FrameworkContext)
  const serverApi = useContext(ServerContext)

  const coins = serverApi.subscriptions
  const tickers = socketApi.tickers
  const exchanges = marketApi.data.exchanges

  const allJobs = serverApi.jobs
  const alertJobs = useMemo<OcoJob[]>(() => allJobs.filter(j => isAlert(j)) as OcoJob[], [allJobs])

  const data = useMemo(
    () =>
      coins.map(coin => {
        const referencePrice = referencePrices[coin.key]
        const ticker = tickers.get(coin.key)
        return {
          ...coin,
          exchangeMeta: exchanges.find(e => e.code === coin.exchange),
          ticker,
          hasAlert: !!alertJobs.find(
            job =>
              job.tickTrigger.exchange === coin.exchange &&
              job.tickTrigger.base === coin.base &&
              job.tickTrigger.counter === coin.counter
          ),
          priceChange: referencePrice
            ? Number(
                (((ticker ? ticker.last : referencePrice) - referencePrice) * 100) / referencePrice
              ).toFixed(2) + "%"
            : "--"
        }
      }),
    [alertJobs, coins, exchanges, referencePrices, tickers]
  )

  return (
    <GetPageVisibility>
      {(visible: boolean) => (
        <RenderIf condition={visible}>
          <Section id="coinList" heading="Coins" nopadding buttons={buttons}>
            <Coins
              data={data}
              onRemove={(coin: Coin) => serverApi.removeSubscription(coin)}
              onClickAlerts={frameworkApi.setAlertsCoin}
              onClickReferencePrice={frameworkApi.setReferencePriceCoin}
            />
          </Section>
        </RenderIf>
      )}
    </GetPageVisibility>
  )
}

const ConnectedCoinsContainer = connect(state => {
  return {
    referencePrices: state.coins.referencePrices
  }
})(CoinsCointainer)

export default ConnectedCoinsContainer
