import React, { Component } from "react"

import { ThemeProvider } from "styled-components"
import theme from "./theme"
import { GlobalStyle } from "./theme"

import { Provider as ReduxProvider } from "react-redux"
import { compose, createStore, applyMiddleware, combineReducers } from "redux"

import createHistory from "history/createBrowserHistory"
import {
  ConnectedRouter,
  connectRouter,
  routerMiddleware
} from "connected-react-router"

import { enableBatching } from "redux-batched-actions"

import thunk from "redux-thunk"
import * as reducers from "./store/reducers"
import * as socket from "./store/socket/connect"

import ErrorContainer from "./containers/ErrorContainer"
import AuthContainer from "./containers/AuthContainer"
import Framework from "./Framework"

const history = createHistory()

const store = createStore(
  enableBatching(connectRouter(history)(combineReducers(reducers))),
  compose(
    applyMiddleware(routerMiddleware(history), thunk.withExtraArgument(socket))
  )
)

socket.initialise(store, history)

export default class App extends Component {
  render() {
    return (
      <ThemeProvider theme={theme}>
        <ReduxProvider store={store}>
          <>
            <GlobalStyle />
            <ErrorContainer />
            <ConnectedRouter history={history}>
              <Framework />
            </ConnectedRouter>
            <AuthContainer />
          </>
        </ReduxProvider>
      </ThemeProvider>
    )
  }
}
