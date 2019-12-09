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
import React, { useEffect } from "react"

import theme from "./theme"
import { ThemeProvider } from "styled-components"
import { GlobalStyle } from "./theme"
import { Loader, Dimmer } from "semantic-ui-react"

import { Provider as ReduxProvider } from "react-redux"
import { compose, createStore, applyMiddleware } from "redux"
import thunk from "redux-thunk"
import { enableBatching } from "redux-batched-actions"
import { createBrowserHistory } from "history"
import { ConnectedRouter, routerMiddleware } from "connected-react-router"

import createRootReducer from "./store/reducers"

import Loadable from "react-loadable"
import Authoriser, { AuthContext, AuthApi } from "modules/auth"
import Socket from "modules/socket"

import * as coinActions from "store/coins/actions"
import * as scriptActions from "store/scripting/actions"
import * as supportActions from "store/support/actions"
import * as errorActions from "store/error/actions"
import { useInterval } from "modules/common/util/hookUtils"
import { LogApi, LogContext, LogManager } from "modules/log"
import { MarketManager } from "modules/market"
import Server from "modules/server"

const history = createBrowserHistory()

// TODO Slowly removing dependency on redux. In the meantime there's a lot of
// wiring here and in Socket to keep things working as I chop stuff out of
// the global store into standalone contexts.
const store = createStore(
  enableBatching(createRootReducer(history)),
  compose(
    applyMiddleware(routerMiddleware(history)),
    applyMiddleware(thunk)
  )
)

const ErrorContainer = Loadable({
  loader: () => import("./containers/ErrorContainer"),
  loading: () => (
    <Dimmer active={true}>
      <Loader active={true} />
    </Dimmer>
  )
})

const FrameworkContainer = Loadable({
  loader: () => import("./FrameworkContainer"),
  loading: () => (
    <Dimmer active={true}>
      <Loader active={true} />
    </Dimmer>
  )
})

/**
 * Wiring to hook up Authoriser, Socket and what's left in the redux store.
 *
 * @param param0
 */
const StoreManagement: React.FC<{ auth: AuthApi; logApi: LogApi }> = ({ auth, logApi }) => {
  // Load state on successful authorisation
  const logTrace = logApi.trace
  useEffect(() => {
    const syncFunction: any = () => {
      return async (dispatch, getState) => {
        await logTrace("Fetching server status")
        var releasesPromise = dispatch(supportActions.fetchReleases(auth))
        var scriptsPromise = dispatch(scriptActions.fetch(auth))
        var metaPromise = dispatch(supportActions.fetchMetadata(auth))
        await dispatch(coinActions.fetchReferencePrices(auth))
        await scriptsPromise
        await metaPromise
        await releasesPromise
      }
    }
    if (auth.authorised) {
      store.dispatch(syncFunction())
    }
  }, [auth, logTrace])

  // Periodically check for new versions.
  useInterval(() => {
    store.dispatch(supportActions.fetchReleases(auth))
  }, 180000)

  return <></>
}

/**
 * Wires context data into StoreManagement.
 */
const ConnectedStoreManagement: React.FC<any> = () => (
  <AuthContext.Consumer>
    {(auth: AuthApi) => (
      <LogContext.Consumer>
        {(logApi: LogApi) => <StoreManagement auth={auth} logApi={logApi} />}
      </LogContext.Consumer>
    )}
  </AuthContext.Consumer>
)

const App: React.FC<any> = () => (
  <ThemeProvider theme={theme}>
    <>
      <GlobalStyle />
      <LogManager>
        <ReduxProvider store={store}>
          <ErrorContainer />
          <Authoriser onError={message => store.dispatch(errorActions.setForeground(message))}>
            <>
              <ConnectedStoreManagement />
              <MarketManager>
                <Server>
                  <Socket getLocation={() => store.getState().router.location}>
                    <ConnectedRouter history={history}>
                      <FrameworkContainer />
                    </ConnectedRouter>
                  </Socket>
                </Server>
              </MarketManager>
            </>
          </Authoriser>
        </ReduxProvider>
      </LogManager>
    </>
  </ThemeProvider>
)

export default App
