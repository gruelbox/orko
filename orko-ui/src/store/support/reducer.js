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
import Immutable from "seamless-immutable"
import * as types from "./actionTypes"
import * as compareVersions from "compare-versions"
import { getFromLS, saveToLS } from "modules/common/util/localStorage"

const loadedIgnoredVersion = getFromLS("ignoredVersion")

export default function reduce(
  state = {
    meta: { version: "0.0.0" },
    releases: [],
    ignoredVersion: !loadedIgnoredVersion ? "0.0.0" : loadedIgnoredVersion,
    hideReleases: false
  },
  action = {}
) {
  switch (action.type) {
    case types.SET_META:
      return Immutable.merge(state, { meta: action.payload })
    case types.HIDE_RELEASES:
      return Immutable.merge(state, {
        hideReleases: true
      })
    case types.SET_IGNORED_VERSION:
      const ignoredVersion = state.releases
        .map(r => r.name)
        .reduce((a, b) => (compareVersions(a, b) === 1 ? a : b))
      saveToLS("ignoredVersion", ignoredVersion)
      return Immutable.merge(state, { ignoredVersion })
    case types.SET_RELEASES:
      return Immutable.merge(state, {
        releases: action.payload
          .filter(r => !r.prerelease)
          .map(r => ({
            name: r.tag_name,
            body: r.body
          }))
      })
    default:
      return state
  }
}
