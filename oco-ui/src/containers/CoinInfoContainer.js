import React from 'react';
import { connect } from 'react-redux';
import CoinInfo from '../components/CoinInfo';
import { coin } from '../store/coin/reducer';
import * as coinActions from '../store/coin/actions';

const TICK_TIME = 5000;

class CoinInfoContainer extends React.Component {

  constructor(props) {
    super(props);
    this.coin = coin(
      this.props.match.params.exchange,
      this.props.match.params.counter,
      this.props.match.params.base
    );
  }

  tick = () => {
    this.props.dispatch(coinActions.fetchTicker(this.coin));
    this.props.dispatch(coinActions.fetchBalance(this.coin));
  }

  componentDidMount() {
    this.tick();
    this.interval = setInterval(this.tick, TICK_TIME);
  }

  componentWillUnmount() {
    clearInterval(this.interval);
  }

  render() {
    return (
      <CoinInfo
        coin={this.coin}
        balance={this.props.balance}
        ticker={this.props.ticker}
        onClickNumber={number => this.props.dispatch(this.props.updateFocusedField(String(number)))}
      />
    );
  }
};

function mapStateToProps(state) {
  return {
    balance: state.coin.balance,
    ticker: state.coin.ticker,
    updateFocusedField: state.focus.updateAction
  };
}

export default connect(mapStateToProps)(CoinInfoContainer);