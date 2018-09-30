import React, { Component } from "react"

import { connect } from "react-redux"

import Section from "../components/primitives/Section"
import Modal from "../components/primitives/Modal"
import Href from "../components/primitives/Href"
import * as uiActions from "../store/ui/actions"
import * as coinsActions from "../store/coins/actions"
import * as focusActions from "../store/focus/actions"
import { Icon } from "semantic-ui-react"
import Input from "../components/primitives/Input.js"
import Form from "../components/primitives/Form"
import Button from "../components/primitives/Button"
import { isValidNumber, formatMoney } from "../util/numberUtils"

class SetReferencePriceContainer extends Component {
  state = {
    price: ""
  }
  
  onChangePrice = (e) => {
    this.setState({ price: e.target.value })
  }

  onSubmit = coinContainer => {
    this.props.dispatch(coinsActions.setReferencePrice(this.props.coin, this.state.price))
    this.props.dispatch(uiActions.closeReferencePrice())
    this.setState({ price: "" })
  }

  onFocus = () => {
    this.props.dispatch(focusActions.setUpdateAction(value => this.setState({ price: value })))
  }

  render() {
    if (!this.props.coin)
      return null
    const ready = this.state.price && isValidNumber(this.state.price) && this.state.price > 0
    return (
      <Modal mobile={this.props.mobile}>
        <Section
          id="referencePrice"
          heading={"Set reference price for " + this.props.coin.name}
          buttons={() => (
            <Href
              title="Close"
              onClick={() => this.props.dispatch(uiActions.closeReferencePrice())}
            >
              <Icon fitted name="close" />
            </Href>
          )}
        > 
          <Form>
            <Input
              id="price"
              error={ready}
              label="Reference price"
              type="number"
              placeholder="Enter price..."
              value={this.state.price ? this.state.price : this.props.referencePrice}
              onChange={this.onChangePrice}
              onFocus={this.onFocus}
            />
            <Button disabled={!ready} onClick={this.onSubmit}>
              Set
            </Button>
          </Form>
        </Section>
      </Modal>
    )
  }
}

function mapStateToProps(state) {
  return {
    coin: state.ui.referencePriceCoin,
    referencePrice: state.ui.referencePriceCoin ? formatMoney(state.coins.referencePrices[state.ui.referencePriceCoin.key], state.ui.referencePriceCoin.counter, "") : null
  }
}

export default connect(mapStateToProps)(SetReferencePriceContainer)