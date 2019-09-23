/*
 * Orko
 * Copyright © 2018-2019 Graham Crockford
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
import React, { Component } from "react"

import { connect } from "react-redux"

import Section, {
  Provider as SectionProvider
} from "../components/primitives/Section"
import Window from "../components/primitives/Window"
import * as uiActions from "../store/ui/actions"
import * as coinsActions from "../store/coins/actions"
import * as focusActions from "../store/focus/actions"
import Input from "../components/primitives/Input.js"
import Form from "../components/primitives/Form"
import Button from "../components/primitives/Button"
import { isValidNumber, formatNumber } from "@orko-ui-common/util/numberUtils"
import { withAuth } from "@orko-ui-auth/index"

class SetReferencePriceContainer extends Component {
  state = {
    price: ""
  }

  onChangePrice = e => {
    this.setState({ price: e.target.value })
  }

  onSubmit = () => {
    this.props.dispatch(
      coinsActions.setReferencePrice(
        this.props.auth,
        this.props.coin,
        this.state.price
      )
    )
    this.props.dispatch(uiActions.closeReferencePrice())
    this.setState({ price: "" })
  }

  onClear = () => {
    this.props.dispatch(
      coinsActions.setReferencePrice(this.props.auth, this.props.coin, null)
    )
    this.props.dispatch(uiActions.closeReferencePrice())
    this.setState({ price: "" })
  }

  onFocus = () => {
    this.props.dispatch(
      focusActions.setUpdateAction(value => this.setState({ price: value }))
    )
  }

  render() {
    if (!this.props.coin) return null
    const ready =
      this.state.price &&
      isValidNumber(this.state.price) &&
      this.state.price > 0
    return (
      <Window mobile={this.props.mobile}>
        <SectionProvider
          value={{
            draggable: !this.props.mobile,
            onHide: () => this.props.dispatch(uiActions.closeReferencePrice())
          }}
        >
          <Section
            id="referencePrice"
            heading={"Set reference price for " + this.props.coin.name}
          >
            <Form
              buttons={() => (
                <>
                  <Button data-orko="doClear" onClick={this.onClear}>
                    Clear
                  </Button>
                  <Button
                    data-orko="doSubmit"
                    disabled={!ready}
                    onClick={this.onSubmit}
                  >
                    Set
                  </Button>
                </>
              )}
            >
              <Input
                id="price"
                error={ready}
                label="Reference price"
                type="number"
                placeholder="Enter price..."
                value={
                  this.state.price
                    ? this.state.price
                    : this.props.referencePrice
                }
                onChange={this.onChangePrice}
                onFocus={this.onFocus}
              />
            </Form>
          </Section>
        </SectionProvider>
      </Window>
    )
  }
}

function mapStateToProps(state) {
  const coinMetadata =
    state.coins.meta && state.ui.referencePriceCoin
      ? state.coins.meta[state.ui.referencePriceCoin.key]
      : undefined
  const priceScale = coinMetadata ? coinMetadata.priceScale : 8
  const referencePrice = state.ui.referencePriceCoin
    ? state.coins.referencePrices[state.ui.referencePriceCoin.key]
    : null
  return {
    coin: state.ui.referencePriceCoin,
    referencePrice: formatNumber(referencePrice, priceScale, "")
  }
}

export default withAuth(connect(mapStateToProps)(SetReferencePriceContainer))
