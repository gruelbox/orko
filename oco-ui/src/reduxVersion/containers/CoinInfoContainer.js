import React from 'react';
import { connect } from 'react-redux';
import * as coinActions from '../store/coin/actions';
import CoinInfoComponent from '../components/CoinInfoComponent';

const CoinInfoContainer = props => (
  <CoinInfoComponent
    coin={props.coin}
    balance={props.balance}
    ticker={props.ticker}
    onClickNumber={number => props.dispatch(props.updateFocusedField(String(number)))}
  />
);

function mapStateToProps(state) {
  return {
    coin: state.coin.coin,
    balance: state.coin.balance,
    ticker: state.coin.ticker,
    updateFocusedField: state.focus.updateAction
  };
}

export default connect(mapStateToProps)(CoinInfoContainer);