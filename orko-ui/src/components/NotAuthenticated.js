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
      . Not got a {exchange.name} account?{" "}
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
