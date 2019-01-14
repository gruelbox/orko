import React from "react"
import Para from "./primitives/Para"
import Href from "./primitives/Href"
import { WIKI_URL } from "../util/support"

export default ({ exchange }) => (
  <>
    <Para color="deemphasis">
      No API details have been provided for {exchange.name}, so this information
      is not available. See the{" "}
      <Href paragraph href={WIKI_URL}>
        Wiki
      </Href>{" "}
      for more information. Not got a {exchange.name} account?{" "}
      <Href paragraph href={exchange.refLink}>
        Sign up
      </Href>
      . Using this link may give the Orko project a small commission. It doesn't
      cost you anything.{" "}
      <Href paragraph href={WIKI_URL + "/Financial-Support"}>
        Read more
      </Href>{" "}
      about supporting the Orko project.
    </Para>
  </>
)
