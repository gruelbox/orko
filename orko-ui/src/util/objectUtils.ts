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
export function areEqualShallow(a: object, b: object): boolean {
  for (var key in a) {
    if (!(key in b) || a[key] !== b[key]) {
      return false
    }
  }
  for (key in b) {
    if (!(key in a) || a[key] !== b[key]) {
      return false
    }
  }
  return true
}

export function replaceInArray(
  arr: ReadonlyArray<any>,
  replacement: any,
  find: any
): ReadonlyArray<any> {
  const result = []
  var found = false
  for (const o of arr) {
    if (find(o)) {
      result.push(replacement)
      found = true
    } else {
      result.push(o)
    }
  }
  if (!found) {
    result.push(replacement)
  }
  return result
}

export function isFunction(x: any): boolean {
  return Object.prototype.toString.call(x) === "[object Function]"
}
