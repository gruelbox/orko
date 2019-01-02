import Immutable from "seamless-immutable"
import * as types from "./actionTypes"
import { replaceInArray } from "../../util/objectUtils"

export const newScript = Immutable({
  name: "New script",
  parameters: [],
  script: `function start() {
  return SUCCESS
}`
})

export const newParameter = Immutable({
  name: "",
  description: "",
  default: "",
  mandatory: false
})

const initialState = Immutable({
  scripts: [],
  loaded: false
})

export default function reduce(state = initialState, action = {}) {
  switch (action.type) {
    case types.SET_SCRIPTS:
      return Immutable.merge(state, {
        scripts: action.payload,
        loaded: true
      })
    case types.DELETE_SCRIPT:
      return Immutable.merge(state, {
        scripts: state.scripts.filter(script => script.id !== action.payload)
      })
    case types.ADD_SCRIPT:
      return Immutable.merge(state, {
        scripts: state.scripts.concat([action.payload])
      })
    case types.UPDATE_SCRIPT:
      return Immutable.merge(state, {
        scripts: replaceInArray(
          state.scripts,
          action.payload,
          s => s.id === action.payload.id
        )
      })
    default:
      return state
  }
}
