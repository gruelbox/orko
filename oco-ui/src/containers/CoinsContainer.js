import React from 'react';
import { connect } from 'react-redux';
import * as coinActions from '../store/coin/actions';

import Coins from '../components/Coins'

const CoinsCointainer = props => (
  <Coins coins={props.coins} onClick={coin => props.dispatch(coinActions.setCoin(coin))}/>
);

function mapStateToProps(state) {
  return {
    coins: state.coins
  };
}

export default connect(mapStateToProps)(CoinsCointainer);