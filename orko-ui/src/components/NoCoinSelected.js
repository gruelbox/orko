import React from "react"
import Para from "./primitives/Para"

export default ({ padded }) => (
  <>
    <Para color="deemphasis" p={padded ? 2 : 0}>
      No coin selected
    </Para>
  </>
)
