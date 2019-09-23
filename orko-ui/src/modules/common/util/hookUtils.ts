import { useEffect, useRef, useReducer, useMemo } from "react"
import Immutable from "seamless-immutable"

/**
 * Effect which sets up an interval callback on mount and clears
 * the interval on unmount.  Clears and recreates the interval
 * any time the delay time is changed.
 *
 * @param callback The code to call every delay milliseconds.
 * @param delay The millisecond delay time.
 */
export function useInterval(callback: () => void, delay: number) {
  const savedCallback = useRef<() => void>()

  // Remember the latest callback.
  useEffect(() => {
    savedCallback.current = callback
  }, [callback])

  // Set up the interval.
  useEffect(() => {
    function tick() {
      savedCallback.current()
    }
    if (delay !== null) {
      let id = setInterval(tick, delay)
      return () => clearInterval(id)
    }
  }, [delay])
}

/**
 * Mutator methods returned by useArray.
 */
export interface UseArrayApi<T> {
  add(value: T): void
  clear()
}

/**
 * A state hook which allows an array state to be maintained.
 *
 * @param initial The initial contents for the array.
 * @returns An array containing the current state of the array and a mutator API.
 */
export function useArray<T>(initial: Array<T>): [Array<T>, UseArrayApi<T>] {
  const [value, dispatch] = useReducer(
    (state: Array<T>, { type, value }: { type: string; value?: T }) => {
      switch (type) {
        case "CLEAR":
          return Immutable([])
        case "ADD":
          return value ? Immutable([value]).concat(state) : state
        default:
          return state
      }
    },
    Immutable(initial)
  )
  const api: UseArrayApi<T> = useMemo(
    () => ({
      add: (value: T) => dispatch({ type: "ADD", value }),
      clear: () => dispatch({ type: "CLEAR" })
    }),
    [dispatch]
  )
  return [value, api]
}
