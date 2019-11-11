import { useEffect, useRef, useReducer, useMemo, DependencyList } from "react"
import Immutable from "seamless-immutable"

/**
 * Effect which sets up an interval callback on mount and clears
 * the interval on unmount.  Clears and recreates the interval
 * any time the delay time is changed.
 *
 * @param callback The code to call every delay milliseconds.
 * @param delay The millisecond delay time.
 */
export function useInterval(callback: () => void, delay: number, dependencies: DependencyList = []) {
  const savedCallback = useRef<() => void>()

  // Remember the latest callback.
  useEffect(() => {
    savedCallback.current = callback
    // eslint-disable-next-line
  }, [callback].concat(dependencies))

  // Set up the interval.

  useEffect(() => {
    function tick() {
      savedCallback.current()
    }
    if (delay !== null) {
      let id = setInterval(tick, delay)
      return () => clearInterval(id)
    }
    // eslint-disable-next-line
  }, [delay].concat(dependencies))
}

/**
 * Mutator methods returned by useArray.
 */
export interface UseArrayApi<T> {
  unshift(value: T, param?: MutateParam<T>): void
  clear(): void
}

export interface MutateParam<T> {
  maxLength?: number
  skipIfAnyMatch?: (existing: T) => boolean
}

type ActionType = "CLEAR" | "UNSHIFT"

/**
 * A state hook which allows an array state to be maintained.
 *
 * @param initial The initial contents for the array.
 * @returns An array containing the current state of the array and a mutator API.
 */
export function useArray<T>(initial: Array<T>): [Array<T>, UseArrayApi<T>] {
  const [value, dispatch] = useReducer(
    (state: Array<T>, { type, value, param }: { type: ActionType; value?: T; param?: MutateParam<T> }) => {
      switch (type) {
        case "CLEAR":
          return Immutable([])
        case "UNSHIFT":
          if (value === undefined) {
            return state
          } else {
            if (!(state instanceof Array)) {
              return Immutable([value])
            } else {
              if (param && param.skipIfAnyMatch && state.some(param.skipIfAnyMatch)) {
                return state
              } else if (param && param.maxLength) {
                return Immutable([value]).concat(state.slice(0, param.maxLength))
              } else {
                return Immutable([value]).concat(state)
              }
            }
          }
        default:
          return state
      }
    },
    Immutable(initial)
  )
  const api: UseArrayApi<T> = useMemo(
    () => ({
      unshift: (value: T, param?: MutateParam<T>) => dispatch({ type: "UNSHIFT", value, param }),
      clear: () => dispatch({ type: "CLEAR" })
    }),
    [dispatch]
  )
  return [value, api]
}
