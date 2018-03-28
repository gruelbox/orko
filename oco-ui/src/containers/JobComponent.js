import React, { Component } from 'react';
import { Tab } from 'semantic-ui-react';
import AlertComponent from '../components/AlertComponent';
import { alertShape } from '../componentsAlertComponent';
import LimitOrderComponent from '../componentsLimitOrderComponent';
import { limitOrderShape } from '../componentsAlertComponent';

const JobComponent = props => (
  <Tab panes={[
    { menuItem: 'Buy', render: () => 
      <Tab.Pane>
        <SubmitLimitTrade direction={BUY} coin={coin} marketPrice={ticker.ask} setBidPrice={setBidPrice} setAskPrice={setAskPrice} />
      </Tab.Pane>
    },
    { menuItem: 'Sell', render: () =>
      <Tab.Pane>
        <SubmitLimitTrade direction={SELL} coin={coin} marketPrice={ticker.bid} setBidPrice={setBidPrice} setAskPrice={setAskPrice}/>
      </Tab.Pane>
    },
    { menuItem: 'Stop Buy', render: () =>
      <Tab.Pane>
        <SubmitStop direction={BUY} coin={coin} marketPrice={ticker.bid} setBidPrice={setBidPrice} setAskPrice={setAskPrice}/>
      </Tab.Pane>
    },
    { menuItem: 'Stop Sell', render: () =>
      <Tab.Pane>
        <SubmitStop direction={SELL} coin={coin} marketPrice={ticker.bid} setBidPrice={setBidPrice} setAskPrice={setAskPrice}/>
      </Tab.Pane>
    },
    { menuItem: 'Alert', render: () =>
      <Tab.Pane>
        <SubmitAlert coin={coin} marketPrice={ticker.bid}/>
      </Tab.Pane>
    }
  ]} />
);

export default JobComponent;

JobComponent.propTypes = {
  jobs: PropTypes.shape({
    alert: PropTypes.shape(alertShape),
    limitOrder: PropTypes.shape(limitOrderShape),
  }).isRequired,
  onChange: PropTypes.func
};