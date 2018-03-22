export function coin(exchange, counter, base) {
  return augmentCoin({
    counter: counter,
    base: base
  }, exchange);
}

export function augmentCoin(p, exchange) {
  return {
    ...p,
    exchange: exchange,
    key: exchange + "/" + p.counter + "/" + p.base,
    name: p.base + "/" + p.counter + " (" + exchange + ")",
    shortName: p.base + "/" + p.counter
  }
}