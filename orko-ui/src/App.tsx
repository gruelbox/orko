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

import { ThemeProvider } from "styled-components"
import theme from "./theme"
import { GlobalStyle } from "./theme"
import { Loader, Dimmer } from "semantic-ui-react"

import { Provider as ReduxProvider } from "react-redux"
import { compose, createStore, applyMiddleware } from "redux"

import createHistory from "history/createBrowserHistory"
import { ConnectedRouter, routerMiddleware } from "connected-react-router"
import Loadable from "react-loadable"

import { enableBatching } from "redux-batched-actions"

import thunk from "redux-thunk"
import createRootReducer from "./store/reducers"
import * as socket from "./store/socket/connect"

import Authoriser from "@orko-ui-auth/containers/Authoriser"

import * as notificationActions from "./store/notifications/actions"
import * as coinActions from "./store/coins/actions"
import * as scriptActions from "./store/scripting/actions"
import * as supportActions from "./store/support/actions"
import * as exchangesActions from "./store/exchanges/actions"

const history = createHistory()

const store = createStore(
  enableBatching(createRootReducer(history)),
  compose(
    applyMiddleware(routerMiddleware(history), thunk.withExtraArgument(socket))
  )
)

const App: React.FC<any> = () => {
  useEffect(() => {
    socket.initialise(store, history)
  }, [])

  const connect = function() {
    return async (dispatch, getState, socket) => {
      await dispatch(notificationActions.trace("Connecting"))
      var scriptsPromise = dispatch(scriptActions.fetch())
      var metaPromise = dispatch(supportActions.fetchMetadata())
      await dispatch(exchangesActions.fetchExchanges())
      await dispatch(coinActions.fetch())
      await dispatch(coinActions.fetchReferencePrices())
      await scriptsPromise
      await metaPromise
      await socket.connect()
    }
  }

  const disconnect = function() {
    return async (dispatch, getState, socket) => {
      await dispatch(notificationActions.trace("Disconnecting"))
      await socket.disconnect()
    }
  }

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

  return (
    <ThemeProvider theme={theme}>
      <>
        <GlobalStyle />
        <ReduxProvider store={store}>
          <Authoriser
            onConnect={() => store.dispatch(connect())}
            onDisconnect={() => store.dispatch(disconnect())}
            render={({ logout, clearWhitelisting }) => (
              <>
                <ErrorContainer />
                <ConnectedRouter history={history}>
                  <FrameworkContainer
                    onLogout={logout}
                    onClearWhitelisting={clearWhitelisting}
                  />
                </ConnectedRouter>
              </>
            )}
          />
        </ReduxProvider>
      </>
    </ThemeProvider>
  )
}

export default App
