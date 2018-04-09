import React from 'react';
import { connect } from 'react-redux';

import { Icon } from 'semantic-ui-react';

import Section from '../components/primitives/Section';
import Para from '../components/primitives/Para';
import Panel from '../components/primitives/Panel';
import Table from '../components/primitives/Table';
import Cell from '../components/primitives/Cell';
import HeaderCell from '../components/primitives/HeaderCell';
import Row from '../components/primitives/Row';
import Href from '../components/primitives/Href';
import Loading from '../components/primitives/Loading';

import * as coinActions from '../store/coin/actions';

const TICK_TIME = 10000;

const NoData = props => (
  <Panel>
    <Para>No market data for {props.coin.name}</Para>
  </Panel>
)

const Order = props => (
  <Row>
    <Cell>
      <Href onClick={props.onRemove}>
        <Icon name="close"/>
      </Href>
    </Cell>
    <Cell>{formatDate(props.order.timestamp)}</Cell>
    <Cell>Unknown {props.order.type}</Cell>
    <Cell number>{props.order.originalAmount}</Cell>
    <Cell number>{props.order.cumulativeAmount}</Cell>
  </Row>
)

const formatDate = (timestamp) => {
  var d = new Date(timestamp);
  return d.toLocaleDateString() + " " + d.toLocaleTimeString();
}

const Orders = props => (
  <div>
    <Table>
      <Row>
        <HeaderCell>
          <Icon name="close"/>
        </HeaderCell>
        <HeaderCell>Created</HeaderCell>
        <HeaderCell>Type</HeaderCell>
        <HeaderCell number>Amount</HeaderCell>
        <HeaderCell number>Filled</HeaderCell>
      </Row>
      {props.orders.openOrders.map(o => (
       <Order key={o.id} order={o} />
      ))}
      {props.orders.hiddenOrders.map(o => (
       <Order key={o.id} order={o} />
      ))}
    </Table>
  </div>
)

class OpenOrdersContainer extends React.Component {

  constructor(props) {
    super(props);
    this.state = {loading: true};
  }

  tick = () => {
    this.props.dispatch(coinActions.fetchOrders(this.props.coin));
  }

  componentDidMount() {
    this.tick();
    this.interval = setInterval(this.tick, TICK_TIME);
  }

  componentWillUnmount() {
    clearInterval(this.interval);
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps.coin.key !== this.props.coin.key) {
      this.setState({ loading: true }, () => this.tick());
    } else {
      this.setState({ loading: false });
    }
  }

  render() {

    var component = this.state.loading
      ? <Loading/>
      : this.props.ordersUnavailable
        ? <NoData coin={this.props.coin}/>
        : (!this.props.orders)
          ? <Loading/>
          : <Orders orders={this.props.orders}/>

    return (
      <Section id="orders" heading="Open Orders">
        {component}
      </Section>
    );
  }
}

function mapStateToProps(state) {
  return {
    orders: state.coin.orders,
    ordersUnavailable: state.coin.ordersUnavailable
  };
}

export default connect(mapStateToProps)(OpenOrdersContainer);