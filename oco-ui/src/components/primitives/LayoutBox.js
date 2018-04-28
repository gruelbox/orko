import Resizable from "re-resizable"
import React from 'react'

const LayoutBox = props => (
  <Resizable
    defaultSize={{
      width: props.w,
      height: props.h
    }}
    enable={{
      top: false,
      right: true,
      bottom: true,
      left: false,
      topRight: false,
      bottomRight: true,
      bottomLeft: false,
      topLeft: false
    }}
  >
    <div style={{ height: "100%" }}>{props.children}</div>
  </Resizable>
)

export default LayoutBox