export function ticker(exchange, counter, base) {
  return augmentTicker({
    counter: counter,
    base: base
  }, exchange);
}

export function augmentTicker(p, exchange) {
  return {
    ...p,
    key: exchange + "/" + p.counter + "/" + p.base,
    name: p.base + "/" + p.counter + " (" + exchange + ")",
    shortName: p.base + "/" + p.counter
  }
}