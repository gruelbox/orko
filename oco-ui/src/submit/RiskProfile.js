import React, { Component } from 'react';
import { Form, Input, Button } from 'semantic-ui-react'

export default class RiskProfile extends Component {
  render() {

    const stopSize = !isNaN(this.props.stopPrice)
      ? Math.abs(this.props.entryPrice - this.props.stopPrice)
      : this.props.entryPrice;

    const profitSize = !isNaN(this.props.takeProfitPrice)
      ? Math.abs(this.props.takeProfitPrice - this.props.entryPrice)
      : 0

    const risk = this.props.amount * stopSize;
    const reward = this.props.amount * profitSize;
    const ratio = risk / reward;

    return (
      <Form.Group inline>
        <Form.Field>
          <label>Risk</label>
          <Input type='text' action disabled>
            <input value={isNaN(risk) ? "Unknown" : risk} />
            <Button>Fix</Button>
          </Input>
        </Form.Field> 
        <Form.Field>
          <label>Reward</label>
          <Input type='text' action disabled>
            <input value={isNaN(reward) ? "Unknown" : reward}  />
            <Button>Fix</Button>
          </Input>
        </Form.Field> 
        <Form.Field>
          <label>Ratio %</label>
          <Input type='text' action disabled>
            <input value={isNaN(ratio) ? "Unknown" : ratio}  />
            <Button>Fix</Button>
          </Input>
        </Form.Field> 
      </Form.Group>
    );
  }
}