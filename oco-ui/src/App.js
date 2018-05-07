import React, { Component } from "react"

import { ThemeProvider } from "styled-components"
import theme from "./theme"

import { Provider as ReduxProvider } from "react-redux"
import { compose, createStore, applyMiddleware, combineReducers } from "redux"
import createSagaMiddleware from "redux-saga"
import thunk from "redux-thunk"
import * as reducers from "./store/reducers"
import rootSaga from "./store/sagas"

import ErrorContainer from "./containers/ErrorContainer"
import AuthContainer from "./containers/AuthContainer"
import Framework from "./Framework"

const sagaMiddleware = createSagaMiddleware()
const store = createStore(
  combineReducers(reducers),
  compose(
    applyMiddleware(thunk),
    applyMiddleware(sagaMiddleware)
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
              <Framework />
            </ThemeProvider>
          </AuthContainer>
        </div>
      </ReduxProvider>
    )
  }
}
