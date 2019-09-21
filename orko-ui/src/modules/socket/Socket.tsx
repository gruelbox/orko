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
import React, { useEffect, ReactElement } from "react"
import * as socket from "store/socket/connect"
import * as notificationActions from "store/notifications/actions"
import * as coinActions from "store/coins/actions"
import * as scriptActions from "store/scripting/actions"
import * as supportActions from "store/support/actions"
import * as exchangesActions from "store/exchanges/actions"

export interface SocketProps {
  store
  history
  children(props: SocketRenderProps): ReactElement
}

export interface SocketRenderProps {
  connect(): void
  disconnect(): void
}

const Socket: React.FC<SocketProps> = (props: SocketProps) => {
  useEffect(() => {
    socket.initialise(props.store, props.history)
  })

  const doConnect = function() {
    return async (dispatch, getState) => {
      await dispatch(notificationActions.trace("Connecting"))
      var scriptsPromise = dispatch(scriptActions.fetch())
      var metaPromise = dispatch(supportActions.fetchMetadata())
      await dispatch(exchangesActions.fetchExchanges())
      await dispatch(coinActions.fetch())
      await dispatch(coinActions.fetchReferencePrices())
      await scriptsPromise
      await metaPromise
      await socket.connect()
    }
  }

  const doDisconnect = function() {
    return async (dispatch, getState) => {
      await dispatch(notificationActions.trace("Disconnecting"))
      await socket.disconnect()
    }
  }

  return props.children({
    connect: () => props.store.dispatch(doConnect()),
    disconnect: () => props.store.dispatch(doDisconnect())
  })
}

export default Socket
