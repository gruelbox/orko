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
import * as notificationActions from "../notifications/actions"

export function wrappedRequest(
  apiRequest,
  jsonHandler,
  errorHandler,
  onSuccess
) {
  return async (dispatch, getState) => {
    dispatchWrappedRequest(
      getState().auth,
      dispatch,
      apiRequest,
      jsonHandler,
      errorHandler,
      onSuccess
    )
  }
}

async function dispatchWrappedRequest(
  auth,
  dispatch,
  apiRequest,
  jsonHandler,
  errorHandler,
  onSuccess
) {
  try {
    // Dispatch the request
    const response = await apiRequest(auth)
    if (!response.ok) {
      if (response.status === 403 || response.status === 401) {
        dispatch(notificationActions.trace("Failed API request"))
        // TODO logout()
      } else if (response.status !== 200) {
        // Otherwise, it's an unexpected error
        var errorMessage = null
        try {
          errorMessage = (await response.json()).message
        } catch (err) {
          // No-op
        }
        if (!errorMessage) {
          errorMessage = response.statusText
            ? response.statusText
            : "Server error (" + response.status + ")"
        }
        throw new Error(errorMessage)
      }
    } else {
      if (jsonHandler) dispatch(jsonHandler(await response.json()))
      if (onSuccess) dispatch(onSuccess())
    }
  } catch (error) {
    if (errorHandler) dispatch(errorHandler(error))
  }
}
