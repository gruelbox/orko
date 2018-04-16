import React from "react"
import { Icon } from "semantic-ui-react"

import Href from "./primitives/Href"
import Link from "./primitives/Link"

const CoinLink = props => (
  <div>
    <Href onClick={props.onRemove}>
      <Icon name="close" />
    </Href>
    <Link to={"/coin/" + props.coin.key}>{props.coin.name}</Link>
  </div>
)

export default CoinLink
