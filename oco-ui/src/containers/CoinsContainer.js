import React from "react"
import { connect } from "react-redux"

import { Icon } from "semantic-ui-react"

import * as coinsActions from "../store/coins/actions"

import CoinLink from "../components/CoinLink"
import Section from "../components/primitives/Section"
import Link from "../components/primitives/Link"

import Table from "../components/primitives/Table"
import Cell from "../components/primitives/Cell"
import HeaderCell from "../components/primitives/HeaderCell"
import Row from "../components/primitives/Row"

class CoinsCointainer extends React.Component {
  render() {
    return (
      <Section id="coinList" heading="Coins" bg="backgrounds.1">
        <Table>
          <tbody>
            <Row>
              <HeaderCell>
                <Icon name="close" />
              </HeaderCell>
              <HeaderCell>Pair</HeaderCell>
              <HeaderCell number px={1}>Price</HeaderCell>
              <HeaderCell number px={1}>Balance</HeaderCell>
              <HeaderCell number px={1}>Value</HeaderCell>
            </Row>
            {this.props.coins.map(coin => {
              const ticker = this.props.tickers[coin.key]
              return <CoinLink
                key={coin.key}
                coin={coin}
                onRemove={() => this.props.dispatch(coinsActions.remove(coin))}
                price={ticker ? ticker.last : null}
              />
            })}
            <Row>
              <Cell />
              <Cell>
                <Link to="/addCoin">
                  <Icon name="add" />Add coin
                </Link>
              </Cell>
              <Cell number />
              <Cell number />
              <Cell number />
            </Row>
          </tbody>
        </Table>
      </Section>
    )
  }
}

function mapStateToProps(state) {
  return {
    coins: state.coins.coins,
    tickers: state.ticker.coins
  }
}

export default connect(mapStateToProps)(CoinsCointainer)
