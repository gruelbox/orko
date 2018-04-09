import React from 'react';

import { coin as createCoin } from './store/coin/reducer';

export default class WithCoinParameter extends React.Component {
  render() {

    const coin = this.props.match.params.exchange ? createCoin(
      this.props.match.params.exchange,
      this.props.match.params.counter,
      this.props.match.params.base
    ) : undefined;

    const ChildComponent = this.props.component;

    return <ChildComponent coin={coin} />
  }
}