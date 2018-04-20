import React from "react"
import Immutable from "seamless-immutable"

import Input from "./primitives/Input.js"
import Form from "./primitives/Form"
import Button from "./primitives/Button"

const Alert = props => {
  const valid =
    (props.highPriceValid || props.lowPriceValid) && props.messageValid

  const onChange = props.onChange
    ? (prop, e) =>
        props.onChange(
          Immutable.merge(props.job, {
            [prop]: e.target.value
          })
        )
    : () => {}

  return (
    <Form>
      <Input
        id="highPrice"
        error={!props.highPriceValid}
        label="Price rises above"
        type="number"
        placeholder="Enter price..."
        value={props.job.highPrice ? props.job.highPrice : ""}
        onChange={e => onChange("highPrice", e)}
        onFocus={e => props.onFocus("highPrice")}
      />
      <Input
        id="lowPrice"
        error={!props.lowPriceValid}
        label="Price drops below"
        type="number"
        placeholder="Enter price..."
        value={props.job.lowPrice ? props.job.lowPrice : ""}
        onChange={e => onChange("lowPrice", e)}
        onFocus={e => props.onFocus("lowPrice")}
      />
      <Input
        id="message"
        error={!props.messageValid}
        label="Message"
        type="text"
        placeholder="Enter message..."
        value={props.job.message}
        onChange={e => onChange("message", e)}
      />
      <Button disabled={!valid} onClick={props.onSubmit}>
        Submit
      </Button>
    </Form>
  )
}

export default Alert
