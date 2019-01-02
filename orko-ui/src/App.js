/*-
 * ===============================================================================L
 * Orko UI
 * ================================================================================
 * Copyright (C) 2018 - 2019 Graham Crockford
 * ================================================================================
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * ===============================================================================E
 */
import React, { Component } from "react"

import { ThemeProvider } from "styled-components"
import theme from "./theme"
import { GlobalStyle } from "./theme"

import { Provider as ReduxProvider } from "react-redux"
import { compose, createStore, applyMiddleware } from "redux"

import createHistory from "history/createBrowserHistory"
import { ConnectedRouter, routerMiddleware } from "connected-react-router"

import { enableBatching } from "redux-batched-actions"

import thunk from "redux-thunk"
import createRootReducer from "./store/reducers"
import * as socket from "./store/socket/connect"

import ErrorContainer from "./containers/ErrorContainer"
import AuthContainer from "./containers/AuthContainer"
import FrameworkContainer from "./FrameworkContainer"

import * as authActions from "./store/auth/actions"

const history = createHistory()

const store = createStore(
  enableBatching(createRootReducer(history)),
  compose(
    applyMiddleware(routerMiddleware(history), thunk.withExtraArgument(socket))
  )
)

socket.initialise(store, history)
store.dispatch(authActions.checkWhiteList())

export default class App extends Component {
  render() {
    return (
      <ThemeProvider theme={theme}>
        <ReduxProvider store={store}>
          <>
            <GlobalStyle />
            <ErrorContainer />
            <ConnectedRouter history={history}>
              <FrameworkContainer />
            </ConnectedRouter>
            <AuthContainer />
          </>
        </ReduxProvider>
      </ThemeProvider>
    )
  }
}
