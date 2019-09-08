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
import Authoriser, { AuthContext, AuthApi } from "@orko-ui-auth/Authoriser"
import Socket from "@orko-ui-socket/Socket"

import * as notificationActions from "store/notifications/actions"
import * as coinActions from "store/coins/actions"
import * as scriptActions from "store/scripting/actions"
import * as supportActions from "store/support/actions"
import * as exchangesActions from "store/exchanges/actions"
import * as jobActions from "store/job/actions"
import { useInterval } from "util/hookUtils"

const history = createBrowserHistory()
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

const StoreManagement: React.FC<{ auth: AuthApi }> = ({ auth }) => {
  // Load state on successful authorisation
  useEffect(() => {
    const syncFunction: any = () => {
      return async (dispatch, getState) => {
        await dispatch(notificationActions.trace("Fetching server status"))
        var releasesPromise = dispatch(supportActions.fetchReleases(auth))
        var scriptsPromise = dispatch(scriptActions.fetch(auth))
        var metaPromise = dispatch(supportActions.fetchMetadata(auth))
        await dispatch(exchangesActions.fetchExchanges(auth))
        await dispatch(coinActions.fetch(auth))
        await dispatch(coinActions.fetchReferencePrices(auth))
        await scriptsPromise
        await metaPromise
        await releasesPromise
      }
    }
    if (auth.authorised) {
      store.dispatch(syncFunction())
    }
  }, [auth])

  // Fetch and dispatch the job details on the server.
  // TODO this should really move to the socket, but for the time being
  // we'll fetch it on an interval.
  useInterval(() => {
    store.dispatch(jobActions.fetchJobs(auth))
  }, 5000)

  // Periodically check for new versions.
  useInterval(() => {
    store.dispatch(supportActions.fetchReleases(auth))
  }, 180000)

  return <></>
}

const App: React.FC<any> = () => (
  <ThemeProvider theme={theme}>
    <>
      <GlobalStyle />
      <ReduxProvider store={store}>
        <ErrorContainer />
        <Authoriser>
          <>
            <AuthContext.Consumer>
              {(auth: AuthApi) => <StoreManagement auth={auth} />}
            </AuthContext.Consumer>
            <Socket store={store} history={history}>
              <ConnectedRouter history={history}>
                <FrameworkContainer />
              </ConnectedRouter>
            </Socket>
          </>
        </Authoriser>
      </ReduxProvider>
    </>
  </ThemeProvider>
)

export default App
