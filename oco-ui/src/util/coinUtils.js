import Immutable from "seamless-immutable"

export function coin(exchange, counter, base) {
  return augmentCoin(
    {
      counter: counter,
      base: base
    },
    exchange
  )
}

export function coinFromKey(key) {
  const split = key.split("/")
  return augmentCoin(
    {
      counter: split[1],
      base: split[2]
    },
    split[0]
  )
}

export function augmentCoin(p, exchange) {
  return Immutable.merge(p, {
    exchange: exchange ? exchange : p.exchange,
    key: (exchange ? exchange : p.exchange) + "/" + p.counter + "/" + p.base,
    name: p.base + "/" + p.counter + " (" + exchange + ")",
    shortName: p.base + "/" + p.counter
  })
}