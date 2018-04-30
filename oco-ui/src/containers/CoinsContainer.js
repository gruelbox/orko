import React from "react"
import { connect } from "react-redux"

import { Icon } from "semantic-ui-react"
import ReactTable from "react-table"

import * as coinsActions from "../store/coins/actions"

import Section from "../components/primitives/Section"
import Link from "../components/primitives/Link"
import Href from "../components/primitives/Href"

import Price from "../components/primitives/Price"

const textStyle = {
  textAlign: "left",
}

const numberStyle = {
  textAlign: "right",
}

const CoinsCointainer = ({data, dispatch}) => (
  <Section id="coinList" heading="Coins" nopadding buttons={() => (
    <Link to="/addCoin" color="heading">
      <Icon name="add" />
    </Link>
  )}>
    <ReactTable
      data={data}
      columns={[
        {
          id: "close",
          Header: () => <Icon name="close" />,
          Cell: ({original}) => (
            <Href onClick={() => dispatch(coinsActions.remove(original))}>
              <Icon name="close" />
            </Href>
          ),
          headerStyle: textStyle,
          style: textStyle,
          width: 36
        },
        {
          id: "name",
          Header: "Name",
          Cell: ({original}) => (
            <Link to={"/coin/" + original.key}>{original.name}</Link>
          ),
          headerStyle: textStyle,
          style: textStyle,
          resizable: true
        },
        {
          id: "price",
          Header: "Price",
          Cell: ({original}) => (
            <Price bare>
              {original.ticker ? original.ticker.last : null}
            </Price>
          ),
          headerStyle: numberStyle,
          style: numberStyle,
          resizable: true
        }
      ]}
      showPagination={false}
      resizable={false}
      className="-striped"
      minRows={0}
    />
  </Section>
)

function mapStateToProps(state) {
  return {
    data: state.coins.coins.map(coin => ({
      ...coin,
      ticker: state.ticker.coins[coin.key]
    }))
  }
}

export default connect(mapStateToProps)(CoinsCointainer)