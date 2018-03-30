import React from 'react';
import { connect } from 'react-redux';
import CoinInfo from '../components/CoinInfo';

const CoinInfoContainer = props => (
  <CoinInfo
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