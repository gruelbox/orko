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
import { AuthContextFeatures, AuthContext } from "@orko-ui-auth/Authoriser"

export interface SocketProps {
  store
  history
  children: ReactElement
}

interface SocketPropsInner extends SocketProps {
  auth: AuthContextFeatures
}

const Socket: React.FC<SocketPropsInner> = (props: SocketPropsInner) => {
  useEffect(() => {
    socket.initialise(props.store, props.history)
  }, [props.store, props.history])

  useEffect(() => {
    if (props.auth.authorised) {
      socket.connect()
    } else {
      socket.disconnect()
    }
  }, [props.auth.authorised])

  return props.children
}

const SocketContainer: React.FC<SocketProps> = (props: SocketProps) => (
  <AuthContext.Consumer>
    {(auth: AuthContextFeatures) => <Socket {...props} auth={auth} />}
  </AuthContext.Consumer>
)

export default SocketContainer
