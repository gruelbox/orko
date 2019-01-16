import React from "react"
import Para from "./primitives/Para"
import Href from "./primitives/Href"
import { Button } from "semantic-ui-react"
import { WIKI_URL } from "../util/support"

export default ({ exchange, onEnablePaperTrading }) => (
  <>
    <Para color="deemphasis">
      No API details provided for {exchange.name}. Find out how to add yours on
      the{" "}
      <Href paragraph href={WIKI_URL}>
        Wiki
      </Href>
      .
    </Para>
    <Para color="deemphasis">
      Not got a {exchange.name} account?{" "}
      <Href paragraph href={exchange.refLink}>
        Sign up to {exchange.name}
      </Href>
      .{" "}
      <Href paragraph href={WIKI_URL + "/Financial-Support"}>
        Read more
      </Href>{" "}
      about supporting the Orko project.
    </Para>
    <Button
      secondary
      data-orko="enablePaperTrading"
      onClick={onEnablePaperTrading}
    >
      Enable paper trading
    </Button>
  </>
)
