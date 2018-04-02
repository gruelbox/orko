import React from 'react';
import { Tabs } from 'rebass';
import Tab from './primitives/Tab';
import Panel from './primitives/Panel';

import AlertContainer from '../containers/AlertContainer';

export default class TradeSelector extends React.Component {

  render() {
    return (
      <div>
        <Tabs mb={3}>
          <Tab>
            Limit
          </Tab>
          <Tab>
            Market
          </Tab>
          <Tab>
            Hard Stop
          </Tab>
          <Tab>
            Soft Stop
          </Tab>
          <Tab>
            Stop/Take Profit
          </Tab>
          <Tab selected>
            Alert
          </Tab>
          <Tab>
            Complex
          </Tab>
        </Tabs>
        <Panel>
          <AlertContainer/>
        </Panel>
      </div>
    );
  }
}