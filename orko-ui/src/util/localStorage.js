/*-
 * ===============================================================================L
 * Orko UI
 * ================================================================================
 * Copyright (C) 2018 - 2019 Graham Crockford
 * ================================================================================
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * ===============================================================================E
 */
export function getFromLS(key) {
  let result = getValueFromLS(key)
  if (result === null) return null
  return JSON.parse(result)
}

export function getValueFromLS(key) {
  let ls = null
  if (global.localStorage) {
    try {
      ls = global.localStorage.getItem(key) || null
    } catch (e) {
      /*Ignore*/
    }
  }
  return ls
}

export function saveToLS(key, value) {
  saveValueToLS(key, JSON.stringify(value))
  return value
}

export function saveValueToLS(key, value) {
  if (global.localStorage) {
    global.localStorage.setItem(key, value)
  }
}
