import React, { ReactElement, useMemo, useRef } from "react"
import Immutable from "seamless-immutable"
import { showBrowserNotification } from "@orko-ui-common/util/browserUtils"
import { useArray } from "@orko-ui-common/util/hookUtils"

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
  localError(message: string): void
  localAlert(message: string): void
  localMessage(message: string): void
  trace(message: string): void
  add(entry: LogRequest): void
  clear(): void
}

export const LogContext = React.createContext<LogApi>(null)

export const LogManager: React.FC<{ children: ReactElement }> = ({
  children
}) => {
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
      updateApi.add(last.current)
    },
    [updateApi]
  )

  const methods = useMemo(
    () => ({
      localError: (message: string) => add({ message, level: ERROR }),
      localAlert: (message: string) => add({ message, level: ALERT }),
      localMessage: (message: string) => add({ message, level: INFO }),
      trace: (message: string) => add({ message, level: TRACE }),
      add,
      clear: updateApi.clear
    }),
    [add, updateApi]
  )

  const api = useMemo(
    () => ({
      logs,
      ...methods
    }),
    [logs, methods]
  )

  return <LogContext.Provider value={api}>{children}</LogContext.Provider>
}
