import Immutable from "seamless-immutable"
import * as types from "./actionTypes"

const initialState = Immutable({
  scripts: [
    {
      id: "1",
      name: "Send price notifications",
      script: `var subscription
var count = 0

function start() {
  notifications.alert(parameters.startMessage)
  subscription = events.setTick(
    function(event) {
      notifications.info("Price is " + event.ticker().getLast() + ", count=" + count)
      count++
      if (count >= parameters.pricesToReport) {
        control.done()
      }
    },
    parameters.selectedCoin
  )
  return RUNNING
}

function stop() {
  notifications.alert(parameters.finishMessage)
  events.clear(subscription)
}`,
      parameters: [
        {
          name: "pricesToReport",
          description: "Number of prices to report",
          mandatory: true
        },
        {
          name: "startMessage",
          description: "Startup message",
          mandatory: false,
          default: "Started"
        },
        {
          name: "finishMessage",
          description: "Finish message",
          mandatory: false,
          default: "Finished"
        }
      ]
    }
  ]
})

export default function reduce(state = initialState, action = {}) {
  switch (action.type) {
    case types.ADD_SCRIPT:
      return state
    default:
      return state
  }
}
