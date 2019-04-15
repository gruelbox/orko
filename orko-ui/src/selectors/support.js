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
import { createSelector } from "reselect"
import Immutable from "seamless-immutable"
import * as compareVersions from "compare-versions"

const getReleases = state => state.support.releases
const getIgnoredVersion = state => state.support.ignoredVersion
const getVersion = state => state.support.meta.version

export const getNewVersions = createSelector(
  [getReleases, getIgnoredVersion, getVersion],
  (releases, ignoredVersion, version) =>
    Immutable(
      releases
        .filter(r => compareVersions(r.name, version) === 1)
        .filter(r => compareVersions(r.name, ignoredVersion) === 1)
    )
)
