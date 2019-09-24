import React, { ReactElement, useMemo, useEffect, useContext, useState } from "react"
import Immutable from "seamless-immutable"
import { AuthContext } from "@orko-ui-auth/index"
import { Exchange, Coin } from "./Types"
import exchangesService from "./exchangesService"
import { LogContext } from "@orko-ui-log/index"
import { connect } from "react-redux"
import { getSelectedCoin } from "selectors/coins"

export interface MarketData {
  exchanges: Array<Exchange>
  selectedExchange: Exchange
}

export interface MarketActions {
  refreshExchanges(): Promise<void>
}

export interface MarketApi {
  data: MarketData
  actions: MarketActions
}

export const MarketContext = React.createContext<MarketApi>(null)

const MarketManagerInner: React.FC<{ coin: Coin; children: ReactElement }> = ({ coin, children }) => {
  const authApi = useContext(AuthContext)
  const logApi = useContext(LogContext)
  const [exchanges, setExchanges] = useState<Array<Exchange>>(Immutable([]))

  const errorPopup = logApi.errorPopup
  const trace = logApi.trace
  const authenticatedRequest = authApi.authenticatedRequest
  const refreshExchanges = useMemo(
    () => async () => {
      trace("Fetching exchanges")
      return authenticatedRequest<Array<Exchange>>(() => exchangesService.fetchExchanges())
        .then(exchanges => {
          setExchanges(exchanges)
          trace(exchanges.length + " Exchanges fetched")
        })
        .catch((error: Error) => errorPopup(error.message))
    },
    [authenticatedRequest, setExchanges, errorPopup, trace]
  )

  const selectedExchange = useMemo(() => {
    return !coin ? null : exchanges.find(e => e.code === coin.exchange)
  }, [coin, exchanges])

  const api = useMemo(
    () => ({
      data: {
        exchanges,
        selectedExchange
      },
      actions: {
        refreshExchanges
      }
    }),
    [exchanges, selectedExchange, refreshExchanges]
  )

  // Fetch exchanges on opening
  useEffect(() => {
    refreshExchanges()
  }, [refreshExchanges])

  return <MarketContext.Provider value={api}>{children}</MarketContext.Provider>
}

export const MarketManager: React.FC<{ children: ReactElement }> = connect(state => ({
  coin: getSelectedCoin(state)
}))(MarketManagerInner)
