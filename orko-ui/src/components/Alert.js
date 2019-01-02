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
import React from "react"
import Immutable from "seamless-immutable"

import Input from "./primitives/Input.js"
import Form from "./primitives/Form"
import Button from "./primitives/Button"

const Alert = props => {
  const valid = props.highPriceValid || props.lowPriceValid

  const onChange = props.onChange
    ? (prop, e) =>
        props.onChange(
          Immutable.merge(props.job, {
            [prop]: e.target.value
          })
        )
    : () => {}

  return (
    <Form
      buttons={() => (
        <>
          <Button
            data-orko="doCreateAlert"
            disabled={!valid}
            onClick={props.onSubmit}
          >
            Create Alert
          </Button>
        </>
      )}
    >
      <Input
        id="highPrice"
        error={props.job.highPrice && !props.highPriceValid}
        label="Price rises above"
        type="number"
        placeholder="Enter price..."
        value={props.job.highPrice ? props.job.highPrice : ""}
        onChange={e => onChange("highPrice", e)}
        onFocus={e => props.onFocus("highPrice")}
      />
      <Input
        id="lowPrice"
        error={props.job.lowPrice && !props.lowPriceValid}
        label="Price drops below"
        type="number"
        placeholder="Enter price..."
        value={props.job.lowPrice ? props.job.lowPrice : ""}
        onChange={e => onChange("lowPrice", e)}
        onFocus={e => props.onFocus("lowPrice")}
      />
      <Input
        id="message"
        error={props.job.message && !props.messageValid}
        label="Message"
        type="text"
        placeholder="Enter message..."
        value={props.job.message}
        onChange={e => onChange("message", e)}
        flex="1"
        mr={0}
      />
    </Form>
  )
}

export default Alert
