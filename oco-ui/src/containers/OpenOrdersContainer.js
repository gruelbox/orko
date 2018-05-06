import React from "react"
import { connect } from "react-redux"

import { Icon } from "semantic-ui-react"
import ReactTable from "react-table"

import Section from "../components/primitives/Section"
import Para from "../components/primitives/Para"
import Panel from "../components/primitives/Panel"
import Href from "../components/primitives/Href"
import Loading from "../components/primitives/Loading"
import FlashEntry from "../components/primitives/FlashEntry"

import * as coinActions from "../store/coin/actions"
import * as jobActions from "../store/job/actions"
import * as jobTypes from '../services/jobTypes'
import * as dateUtils from '../util/dateUtils'

const TICK_TIME = 10000

const NoCoin = props => (
  <Panel p={2}>
    <Para>No coin selected</Para>
  </Panel>
)

const NoData = props => (
  <Panel p={2}>
    <Para>No market data for {props.coin.name}</Para>
  </Panel>
)

const NoOrders = props => (
  <Panel p={2}>
    <Para>No open orders</Para>
  </Panel>
)

const textStyle = {
  textAlign: "left"
}

const numberStyle = {
  textAlign: "right"
}

const Orders = props => (
  <ReactTable
    data={props.orders.asMutable()}
    defaultSorted={[
      {
        id: "createdDate",
        desc: false
      }
    ]}
    columns={[
      {
        id: "close",
        Header: () => null,
        Cell: ({ original }) => (
          <FlashEntry>
            <Href onClick={() => props.onCancel(original.id, original.type)} title="Cancel order">
              <Icon fitted name="close" />
            </Href>
          </FlashEntry>
        ),
        headerStyle: textStyle,
        style: textStyle,
        width: 32
      },
      {
        id: "watch",
        Header: () => null,
        Cell: ({ original }) => (
          <FlashEntry>
            <Href onClick={() => props.onWatch(original.id, original.watchJob)} title={original.watchJob ? "Watched" : "Not watched"}>
              <Icon fitted name={original.watchJob ? "eye" : "circle outline"} />
            </Href>
          </FlashEntry>
        ),
        headerStyle: textStyle,
        style: textStyle,
        width: 32
      },
      {
        id: "runningAt",
        Header: "Running",
        Cell: ({ original }) => <FlashEntry content="Exchange" />,
        headerStyle: textStyle,
        style: textStyle,
        resizable: true,
        width: 70
      },
      {
        id: "createdDate",
        Header: "Created",
        Cell: ({ original }) => (
          <FlashEntry content={dateUtils.formatDate(original.timestamp)} />
        ),
        headerStyle: textStyle,
        style: textStyle,
        resizable: true,
        width: 130
      },
      {
        id: "orderType",
        Header: "Type",
        Cell: ({ original }) => (
          <FlashEntry content={original.type === "BID" ? "Buy" : "Sell"} />
        ),
        headerStyle: textStyle,
        style: textStyle,
        resizable: true,
        width: 50
      },
      {
        Header: "Limit",
        Cell: ({ original }) => <FlashEntry content={original.limitPrice} />,
        headerStyle: numberStyle,
        style: numberStyle,
        resizable: true,
        width: 80
      },
      {
        id: "stopPrice",
        Header: "Trigger",
        Cell: ({ original }) => (
          <FlashEntry content={original.stopPrice ? original.stopPrice : "-"} />
        ),
        headerStyle: numberStyle,
        style: numberStyle,
        resizable: true,
        width: 80
      },
      {
        Header: "Amount",
        Cell: ({ original }) => (
          <FlashEntry content={original.originalAmount} />
        ),
        headerStyle: numberStyle,
        style: numberStyle,
        resizable: true,
        width: 80
      },
      {
        Header: "Filled",
        Cell: ({ original }) => (
          <FlashEntry content={original.cumulativeAmount} />
        ),
        headerStyle: numberStyle,
        style: numberStyle,
        resizable: true,
        width: 80
      }
    ]}
    showPagination={false}
    resizable={false}
    className="-striped"
    minRows={0}
    noDataText="No open orders"
  />
)

class OpenOrdersContainer extends React.Component {
  constructor(props) {
    super(props)
    this.state = { loading: true }
  }

  tick = () => {
    this.props.dispatch(coinActions.fetchOrders(this.props.coin))
  }

  onCancel = (id, orderType) => {
    this.props.dispatch(coinActions.cancelOrder(this.props.coin, id, orderType))
  }

  onWatch = (id, watchJob) => {
    if (watchJob) {
      this.props.dispatch(jobActions.deleteJob(watchJob))
    } else {
      this.props.dispatch(jobActions.submitWatchJob(this.props.coin, id))
    }
  }

  componentDidMount() {
    this.tick()
    this.interval = setInterval(this.tick, TICK_TIME)
  }

  componentWillUnmount() {
    clearInterval(this.interval)
  }

  componentWillReceiveProps(nextProps) {
    const nextKey = nextProps.coin ? nextProps.coin.key : null
    const thisKey = this.props.coin ? this.props.coin.key : null
    if (nextKey !== thisKey) {
      this.setState({ loading: true }, () => this.tick())
    } else {
      this.setState({ loading: false })
    }
  }

  render() {
    var component = this.state.loading ? (
      <Loading p={2} />
    ) : !this.props.coin ? (
      <NoCoin />
    ) : this.props.ordersUnavailable ? (
      <NoData coin={this.props.coin} />
    ) : !this.props.orders ? (
      <Loading />
    ) : this.props.orders.length === 0 ? (
      <NoOrders />
    ) : (
      <Orders orders={this.props.orders} onCancel={this.onCancel} onWatch={this.onWatch} />
    )

    return (
      <Section nopadding id="orders" heading="Open Orders">
        {component}
      </Section>
    )
  }
}

function mapStateToProps(state, props) {
  const notifierJobs =
    state.job.jobs && props.coin
      ? state.job.jobs.filter(
          job =>
            job.jobType === jobTypes.WATCH_JOB &&
            job.tickTrigger.exchange === props.coin.exchange &&
            job.tickTrigger.base === props.coin.base &&
            job.tickTrigger.counter === props.coin.counter
        )
      : []
  return {
    orders: state.coin.orders
      ? state.coin.orders.allOpenOrders.map(order => {
          const watchJob = notifierJobs.find(job => job.orderId === order.id)
          if (watchJob) {
            return { ...order, watchJob }
          } else {
            return order
          }
        })
      : null,
    ordersUnavailable: state.coin.ordersUnavailable
  }
}

export default connect(mapStateToProps)(OpenOrdersContainer)
