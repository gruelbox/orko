import React, { Component } from 'react';
import Authentication from './Authentication';
import Header from './Header';
import AddCoin from './AddCoin';
import Coin from './Coin';
import Jobs from './job/Jobs';
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
            <div>
              <Switch>
                <Route exact path='/' component={Authentication}/>
                <Route exact path='/addCoin' component={AddCoin}/>
                <Route exact path='/jobs' component={Jobs}/>
                <Route path='/coin/:exchange/:counter/:base' component={Coin}/>
              </Switch>
            </div>
          </div>
        </BrowserRouter>
      </Provider>
    );
  }
}