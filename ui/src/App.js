import React, { Component } from 'react';
import Authentication from './Authentication';
import Header from './Header';
import AddTicker from './AddTicker';
import Ticker from './Ticker';
import { Switch, Route, BrowserRouter } from 'react-router-dom';
import { Provider  } from 'unstated';
import './App.css';

export default class App extends Component {
  render() {
    return (
      <Provider>
        <BrowserRouter>
          <div>
            <Header/>
            <div style={{ marginTop: '4em' }}>
              <Switch>
                <Route exact path='/' component={Authentication}/>
                <Route exact path='/addticker' component={AddTicker}/>
                <Route path='/ticker/:exchange/:counter/:base' component={Ticker}/>
              </Switch>
            </div>
          </div>
        </BrowserRouter>
      </Provider>
    );
  }
}