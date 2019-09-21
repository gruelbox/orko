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
import Authoriser from "@orko-ui-auth/Authoriser"
import Socket from "@orko-ui-socket/Socket"
import { SocketRenderProps } from "modules/socket/Socket"

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

const App: React.FC<any> = () => (
  <ThemeProvider theme={theme}>
    <>
      <GlobalStyle />
      <ReduxProvider store={store}>
        <ErrorContainer />
        <Socket store={store} history={history}>
          {(socket: SocketRenderProps) => (
            <Authoriser
              onConnect={socket ? socket.connect : () => {}}
              onDisconnect={socket ? socket.disconnect : () => {}}
            >
              <ConnectedRouter history={history}>
                <FrameworkContainer />
              </ConnectedRouter>
            </Authoriser>
          )}
        </Socket>
      </ReduxProvider>
    </>
  </ThemeProvider>
)

export default App
