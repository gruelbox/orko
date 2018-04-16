import React, { Component } from "react"

import { Provider as RebassProvider } from "rebass"

import { ThemeProvider } from "styled-components"
import theme from "./theme"

import { Provider as ReduxProvider } from "react-redux"
import { createStore, applyMiddleware, combineReducers } from "redux"
import thunk from "redux-thunk"
import * as reducers from "./store/reducers"

import ErrorContainer from "./containers/ErrorContainer"
import AuthContainer from "./containers/AuthContainer"
import Framework from "./Framework"

const store = createStore(combineReducers(reducers), applyMiddleware(thunk))

export default class App extends Component {
  render() {
    return (
      <ReduxProvider store={store}>
        <RebassProvider>
          <ErrorContainer />
          <AuthContainer>
            <ThemeProvider theme={theme}>
              <Framework />
            </ThemeProvider>
          </AuthContainer>
        </RebassProvider>
      </ReduxProvider>
    )
  }
}
