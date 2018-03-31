import React from 'react';
import { connect } from 'react-redux';
import { BUY } from '../store/limitOrder/reducer';
import * as limitOrderActions from '../store/limitOrder/actions';
import * as focusActions from '../store/focus/actions';
import LimitOrder from '../components/LimitOrder';

const LimitOrderContainer = props => (
  <LimitOrder
    job={props.limitOrder}
    onChange={job => props.dispatch(limitOrderActions.update(job))}
    onFocus={focus => props.dispatch(focusActions.setUpdateAction(value => limitOrderActions.updateProperty(focus, value)))}
    onSetBidPrice={() => props.dispatch(limitOrderActions.updateProperty("price", props.ticker.bid))}
    onSetAskPrice={() => props.dispatch(limitOrderActions.updateProperty("price", props.ticker.ask))}
    onSetMarketPrice={() => props.dispatch(limitOrderActions.updateProperty("price", props.limitOrder.direction === BUY ? props.ticker.ask : props.ticker.bid))}
  />
);

function mapStateToProps(state) {
  return {
    limitOrder: state.limitOrder.job,
    ticker: state.coin.ticker
  };
}

export default connect(mapStateToProps)(LimitOrderContainer);