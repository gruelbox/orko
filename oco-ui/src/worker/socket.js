import { ws } from "../services/fetchUtil"
import * as channelMessages from "./socketMessages"

export default function MyWorker() {
  var socket
  let onmessage = m => {
    console.log(m)
    switch (m.data.action) {
      case channelMessages.CONNECT:
        socket = ws(m.data.data)
        socket.onopen = () => postMessage(channelMessages.OPEN)
        socket.onclose = () => postMessage(channelMessages.CLOSE)
        socket.onmessage = evt => {
          try {
              postMessage(JSON.parse(evt.data))
          } catch (e) {
              console.log("Invalid message from server", evt.data)
          }
        }
        break
      case channelMessages.DISCONNECT:
        socket.close(undefined, "Shutdown", { keepClosed: true })
        break
      default:
        console.log("Unknown message", m)
    }
  }
}