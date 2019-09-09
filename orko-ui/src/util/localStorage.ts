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
const globalAny: any = global

export function getFromLS(key: string): any {
  const result = getValueFromLS(key)
  return result === null ? null : JSON.parse(result)
}

export function getValueFromLS(key: string): string {
  if (!globalAny.localStorage) {
    return null
  }
  try {
    return globalAny.localStorage.getItem(key) || null
  } catch (e) {
    return null
  }
}

export function saveToLS(key: string, value: any): any {
  saveValueToLS(key, JSON.stringify(value))
  return value
}

export function saveValueToLS(key: string, value: string): void {
  if (globalAny.localStorage) {
    globalAny.localStorage.setItem(key, value)
  }
}
