import React from "react"
import { Icon } from "semantic-ui-react"

import Href from "./primitives/Href"
import Link from "./primitives/Link"

import Cell from "../components/primitives/Cell"
import Row from "../components/primitives/Row"
import Price from "./primitives/Price"

const CoinLink = ({ onRemove, onClickNumber, coin, price }) => (
  <Row>
    <Cell>
      <Href onClick={onRemove}>
        <Icon name="close" />
      </Href>
    </Cell>
    <Cell>
      <Link to={"/coin/" + coin.key}>{coin.name}</Link>
    </Cell>
    <Cell number>
      <Price bare>
        {price}
      </Price>
    </Cell>
    <Cell number>
    </Cell>
    <Cell number>
    </Cell>
  </Row>
)

export default CoinLink
