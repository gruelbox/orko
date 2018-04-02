import React from 'react';
import Tab from './primitives/Tab';
import Tabs from './primitives/Tabs';
import Panel from './primitives/Panel';

import AlertContainer from '../containers/AlertContainer';

export default class TradeSelector extends React.Component {

  constructor(props) {
    super(props);
    this.state = { selected: "alert" };
  }

  render() {
    
    var panel = null;
    if (this.state.selected === 'alert') {
      panel = <AlertContainer coin={this.props.coin}/>;
    }

    return (
      <div>
        <Tabs mb={3}>
          <Tab selected={this.state.selected === "limit"} onClick={() => this.setState({selected: "limit"})}>
            Limit
          </Tab>
          <Tab selected={this.state.selected === "market"} onClick={() => this.setState({selected: "market"})}>
            Market
          </Tab>
          <Tab selected={this.state.selected === "hardstop"} onClick={() => this.setState({selected: "hardstop"})}>
            Hard Stop
          </Tab>
          <Tab selected={this.state.selected === "softstop"} onClick={() => this.setState({selected: "softstop"})}>
            Soft Stop
          </Tab>
          <Tab selected={this.state.selected === "oco"} onClick={() => this.setState({selected: "oco"})}>
            Stop/Take Profit
          </Tab>
          <Tab selected={this.state.selected === "alert"} onClick={() => this.setState({selected: "alert"})}>
            Alert
          </Tab>
          <Tab selected={this.state.selected === "complex"} onClick={() => this.setState({selected: "complex"})}>
            Complex
          </Tab>
        </Tabs>
        <Panel>
          {panel}
        </Panel>
      </div>
    );
  }
}