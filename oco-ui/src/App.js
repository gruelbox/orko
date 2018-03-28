import React, { Component } from 'react';
//import Header from './Header';
import { Switch, Route, BrowserRouter } from 'react-router-dom';
import KitchenSink from './KitchenSink';
import './App.css';

import { createStore, applyMiddleware, combineReducers } from 'redux';
import { Provider } from 'react-redux';
import thunk from 'redux-thunk';
import * as reducers from './store/reducers';
import AuthContainer from './containers/AuthContainer';

const store = createStore(combineReducers(reducers), applyMiddleware(thunk));

export default class App extends Component {
  render() {
    return (
      <Provider store={store}>
        <BrowserRouter>
          <div>
            {/* <Header/> */}
            <AuthContainer/>
            <div>
              <Switch>
                <Route exact path='/kitchenSink' component={KitchenSink}/>
                { /* <Route path='/coin/:exchange/:counter/:base' component={Coin}/> */ }
              </Switch>
            </div>
          </div>
        </BrowserRouter>
      </Provider>
    );
  }
}