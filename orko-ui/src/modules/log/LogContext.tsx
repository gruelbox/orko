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
import React, { ReactElement, useMemo, useRef, useState } from "react"
import Immutable from "seamless-immutable"
import { showBrowserNotification } from "modules/common/util/browserUtils"
import { useArray } from "modules/common/util/hookUtils"
import { ErrorPopup } from "./ErrorPopup"

const ERROR = "ERROR"
const ALERT = "ALERT"
const INFO = "INFO"
const TRACE = "TRACE"
export type LogLevel = typeof ERROR | typeof ALERT | typeof INFO | typeof TRACE

export interface LogRequest {
  message: string
  level: LogLevel
}

export interface LogEntry extends LogRequest {
  dateTime: Date
}

export interface LogApi {
  logs: Array<LogEntry>
  errorPopup(message: string): void
  localError(message: string): void
  localAlert(message: string): void
  localMessage(message: string): void
  trace(message: string): void
  add(entry: LogRequest): void
  clear(): void
}

export const LogContext = React.createContext<LogApi>(null)

export const LogManager: React.FC<{ children: ReactElement }> = ({ children }) => {
  const [error, setError] = useState<string>(null)
  const [logs, updateApi] = useArray<LogEntry>([])
  const last = useRef<LogEntry>()
  const add = useMemo(
    () => (request: LogRequest) => {
      if (!last.current || last.current.message !== request.message) {
        if (request.level === ALERT || request.level === ERROR) {
          showBrowserNotification("Orko Client", request.message)
        }
      }
      if ("dateTime" in request) {
        last.current = request as LogEntry
      } else {
        last.current = Immutable.set(Immutable(request), "dateTime", new Date())
      }
      updateApi.unshift(last.current)
    },
    [updateApi]
  )

  const methods = useMemo(
    () => ({
      errorPopup: (message: string) => {
        setError(message)
      },
      localError: (message: string) => add({ message, level: ERROR }),
      localAlert: (message: string) => add({ message, level: ALERT }),
      localMessage: (message: string) => add({ message, level: INFO }),
      trace: (message: string) => add({ message, level: TRACE }),
      add,
      clear: updateApi.clear
    }),
    [add, updateApi, setError]
  )

  const api = useMemo(
    () => ({
      logs,
      ...methods
    }),
    [logs, methods]
  )

  return (
    <LogContext.Provider value={api}>
      <>
        {error !== null ? <ErrorPopup message={error} onClose={() => setError(null)} /> : null}
        {children}
      </>
    </LogContext.Provider>
  )
}
