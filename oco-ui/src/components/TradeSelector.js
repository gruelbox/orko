import React from 'react';
import { Tabs } from 'rebass';
import ForeTab from './primitives/ForeTab';
import Panel from './primitives/Panel';

import AlertContainer from '../containers/AlertContainer';

const TradeSelector = props => (
  <div>
    <Tabs>
      <ForeTab>
        Limit
      </ForeTab>
      <ForeTab>
        Market
      </ForeTab>
      <ForeTab>
        Hard Stop
      </ForeTab>
      <ForeTab>
        Soft Stop
      </ForeTab>
      <ForeTab>
        Stop/Take Profit
      </ForeTab>
      <ForeTab selected>
        Alert
      </ForeTab>
      <ForeTab>
        Complex
      </ForeTab>
    </Tabs>
    <Panel p={2}>
      <AlertContainer/>
    </Panel>
  </div>
);

export default TradeSelector;