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
import React, { useState, useContext, useMemo } from "react"

import { connect } from "react-redux"

import Section, { Provider as SectionProvider } from "../components/primitives/Section"
import Window from "../components/primitives/Window"
import * as coinsActions from "../store/coins/actions"
import Input from "../components/primitives/Input.js"
import Form from "../components/primitives/Form"
import Button from "../components/primitives/Button"
import { isValidNumber, formatNumber } from "modules/common/util/numberUtils"
import { AuthContext } from "modules/auth"
import { FrameworkContext } from "FrameworkContainer"
import { ServerContext } from "modules/server"

interface SetReferencePriceContainerProps {
  mobile: boolean
  dispatch
  referencePrices
}

const SetReferencePriceContainer: React.FC<SetReferencePriceContainerProps> = ({
  mobile,
  dispatch,
  referencePrices
}) => {
  const [price, setPrice] = useState("")
  const frameworkApi = useContext(FrameworkContext)
  const authApi = useContext(AuthContext)
  const serverApi = useContext(ServerContext)

  const coin = frameworkApi.referencePriceCoin
  const coinMetadata = coin ? serverApi.coinMetadata.get(coin.key) : null
  const referencePriceUnformatted = coin ? referencePrices[coin.key] : null
  const referencePrice = useMemo(() => {
    const priceScale = coinMetadata ? coinMetadata.priceScale : 8
    return formatNumber(referencePriceUnformatted, priceScale, "")
  }, [coinMetadata, referencePriceUnformatted])

  if (!coin) return null

  const onSubmit = () => {
    dispatch(coinsActions.setReferencePrice(authApi, coin, Number(price)))
    frameworkApi.setReferencePriceCoin(null)
    setPrice("")
  }

  const onClear = () => {
    dispatch(coinsActions.setReferencePrice(authApi, coin, null))
    frameworkApi.setReferencePriceCoin(null)
    setPrice("")
  }

  const onFocus = () => {
    frameworkApi.setLastFocusedFieldPopulater(setPrice)
  }

  const ready = price && isValidNumber(price) && Number(price) > 0
  return (
    <Window mobile={mobile} large={false}>
      <SectionProvider
        value={{
          draggable: !mobile,
          onHide: () => frameworkApi.setReferencePriceCoin(null)
        }}
      >
        <Section id="referencePrice" heading={"Set reference price for " + coin.name}>
          <Form
            buttons={() => (
              <>
                <Button data-orko="doClear" onClick={onClear}>
                  Clear
                </Button>
                <Button data-orko="doSubmit" disabled={!ready} onClick={onSubmit}>
                  Set
                </Button>
              </>
            )}
          >
            <Input
              id="price"
              error={ready}
              label="Reference price"
              type="number"
              placeholder="Enter price..."
              value={price ? price : referencePrice}
              onChange={e => setPrice(e.target.value)}
              onFocus={onFocus}
            />
          </Form>
        </Section>
      </SectionProvider>
    </Window>
  )
}

function mapStateToProps(state) {
  return {
    referencePrices: state.coins.referencePrices
  }
}

export default connect(mapStateToProps)(SetReferencePriceContainer)
