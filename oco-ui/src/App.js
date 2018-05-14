import React, { Component } from "react"

import { ThemeProvider } from "styled-components"
import theme from "./theme"

import { Provider as ReduxProvider } from "react-redux"
import { compose, createStore, applyMiddleware, combineReducers } from "redux"

import createHistory from 'history/createBrowserHistory'
import { ConnectedRouter, routerReducer, routerMiddleware } from 'react-router-redux'

import createSagaMiddleware from "redux-saga"
import thunk from "redux-thunk"
import * as reducers from "./store/reducers"
import rootSaga from "./store/sagas"

import ErrorContainer from "./containers/ErrorContainer"
import AuthContainer from "./containers/AuthContainer"
import Framework from "./Framework"

const sagaMiddleware = createSagaMiddleware()
const history = createHistory()
const reduxRouterMiddleware = routerMiddleware(history)

const store = createStore(
  combineReducers({
    ...reducers,
    router: routerReducer
  }),
  compose(
    applyMiddleware(reduxRouterMiddleware),
    applyMiddleware(thunk),
    applyMiddleware(sagaMiddleware),
  )
)
sagaMiddleware.run(rootSaga, store.dispatch, store.getState)

export default class App extends Component {
  render() {
    return (
      <ReduxProvider store={store}>
        <div>
          <ErrorContainer />
          <AuthContainer>
            <ThemeProvider theme={theme}>
              <ConnectedRouter history={history}>
                <Framework />
              </ConnectedRouter>
            </ThemeProvider>
          </AuthContainer>
        </div>
      </ReduxProvider>
    )
  }
}
