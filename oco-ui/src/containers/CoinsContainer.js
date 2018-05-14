import React from "react"
import { connect } from "react-redux"

import { Icon } from "semantic-ui-react"
import ReactTable from "react-table"

import * as coinsActions from "../store/coins/actions"
import * as uiActions from "../store/ui/actions"
import { getCoinsForDisplay } from "../selectors/coins"

import Section from "../components/primitives/Section"
import Link from "../components/primitives/Link"
import Href from "../components/primitives/Href"
import Price from "../components/primitives/Price"

const textStyle = {
  textAlign: "left"
}

const numberStyle = {
  textAlign: "right"
}

const CoinsCointainer = ({ data, dispatch, updateFocusedField }) => (
  <Section
    id="coinList"
    heading="Coins"
    nopadding
    buttons={() => (
      <Link to="/addCoin" color="heading">
        <Icon name="add" />
      </Link>
    )}
  >
    <ReactTable
      data={data.asMutable()}
      columns={[
        {
          id: "close",
          Header: null,
          Cell: ({ original }) => (
            <Href
              title="Remove coin"
              onClick={() => dispatch(coinsActions.remove(original))}
            >
              <Icon fitted name="close" />
            </Href>
          ),
          headerStyle: textStyle,
          style: textStyle,
          width: 32,
          sortable: false,
          resizable: false
        },
        {
          id: "exchange",
          Header: "Exchange",
          accessor : "exchange",
          Cell: ({ original }) => (
            <Link to={"/coin/" + original.key} title="Open coin">
              {original.exchange}
            </Link>
          ),
          headerStyle: textStyle,
          style: textStyle,
          resizable: true,
          minWidth: 50,
        },
        {
          id: "name",
          Header: "Name",
          accessor: "shortName",
          Cell: ({ original }) => (
            <Link to={"/coin/" + original.key} title="Open coin">
              {original.shortName}
            </Link>
          ),
          headerStyle: textStyle,
          style: textStyle,
          resizable: true,
          minWidth: 50
        },
        {
          id: "price",
          Header: "Price",
          Cell: ({ original }) => (
            <Price counter={original.counter} bare onClick={number => {
              if (updateFocusedField) {
                updateFocusedField(number)
              }
            }}>
              {original.ticker ? original.ticker.last : undefined}
            </Price>
          ),
          headerStyle: numberStyle,
          style: numberStyle,
          resizable: true,
          minWidth: 50,
          sortable: false,
        },
        {
          id: "alert",
          Header: <Icon fitted name="bell outline" />,
          Cell: ({ original }) => (
            <Href
              title="Manage alerts"
              onClick={() => dispatch(uiActions.openAlerts(original))}
            >
              <Icon
                fitted
                name={original.hasAlert ? "bell" : "bell outline"}
              />
            </Href>
          ),
          headerStyle: textStyle,
          style: textStyle,
          width: 32,
          sortable: false,
          resizable: false
        }
      ]}
      showPagination={false}
      resizable={false}
      className="-striped"
      minRows={0}
      noDataText="Add a coin by clicking +, above"
    />
  </Section>
)

function mapStateToProps(state) {
  return {
    updateFocusedField: state.focus.fn,
    data: getCoinsForDisplay(state)
  }
}

export default connect(mapStateToProps)(CoinsCointainer)
