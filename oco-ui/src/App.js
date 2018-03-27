import React, { Component } from 'react';
import Header from './Header';
import AddCoin from './AddCoin';
import Coin from './Coin';
import Jobs from './job/Jobs';
import { Switch, Route, BrowserRouter } from 'react-router-dom';
import { Provider as UnstatedProvider } from 'unstated';
import { AuthProvider } from './context/AuthContext';
import KitchenSink from './reduxVersion/KitchenSink';
import './App.css';

export default class App extends Component {
  render() {
    return (
      <AuthProvider>
        <UnstatedProvider>
          <BrowserRouter>
            <div>
              <Header/>
              <div>
                <Switch>
                  <Route exact path='/kitchenSink' component={KitchenSink}/>
                  <Route exact path='/addCoin' component={AddCoin}/>
                  <Route exact path='/jobs' component={Jobs}/>
                  <Route path='/coin/:exchange/:counter/:base' component={Coin}/>
                </Switch>
              </div>
            </div>
          </BrowserRouter>
        </UnstatedProvider>
      </AuthProvider>
    );
  }
}