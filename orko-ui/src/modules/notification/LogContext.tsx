import React, { ReactElement, useReducer, useMemo } from "react"
import Immutable from "seamless-immutable"
import { showBrowserNotification } from "util/browserUtils"

const ERROR = "ERROR"
const ALERT = "ALERT"
const INFO = "INFO"
const TRACE = "TRACE"
export type LogLevel = typeof ERROR | typeof ALERT | typeof INFO | typeof TRACE

const ADD = "ADD"
const CLEAR = "CLEAR"
type Action = typeof CLEAR | typeof ADD

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
  // Reducer to manage the array of notifications and sneakily
  // fire off browser notifications as a side-effect
  const [logs, dispatch] = useReducer(
    (
      state: Array<LogEntry>,
      { type, value }: { type: Action; value?: LogRequest }
    ) => {
      switch (type) {
        case CLEAR:
          return Immutable([])
        case ADD:
          if (value) {
            if (state.length === 0 || state[0].message !== value.message) {
              if (value.level === ALERT || value.level === ERROR) {
                showBrowserNotification("Orko Client", value.message)
              }
            }
            const log: LogEntry = Immutable.set(
              Immutable(value),
              "dateTime",
              new Date()
            )
            return Immutable([log]).concat(state)
          } else {
            return state
          }
        default:
          return state
      }
    },
    Immutable([])
  )

  const local = useMemo(
    () => (message: string, level: LogLevel) => {
      dispatch({
        type: ADD,
        value: {
          level,
          message
        }
      })
    },
    [dispatch]
  )

  const methods = useMemo(
    () => ({
      localError: (message: string) => local(message, ERROR),
      localAlert: (message: string) => local(message, ALERT),
      localMessage: (message: string) => local(message, INFO),
      trace: (message: string) => local(message, TRACE),
      add: (entry: LogRequest) => {
        console.log("add", entry)
        dispatch({
          type: ADD,
          value: entry
        })
      },
      clear: () =>
        dispatch({
          type: CLEAR
        })
    }),
    [local, dispatch]
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
