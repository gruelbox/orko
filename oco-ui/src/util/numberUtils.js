export const isValidNumber = val => !isNaN(val) && val !== ""

const formatters = {
  BTC: x => Number(x).toFixed(8),
  ETH: x => Number(x).toFixed(7),
  USDT: x => Number(x).toFixed(2),
  USD: x => Number(x).toFixed(2),
  EUR: x => Number(x).toFixed(2),
  XXX: x => x
}

export const formatMoney = (x, currency, undefinedValue) => {
  if (!isValidNumber(x)) return undefinedValue
  var formatter = formatters[currency]
  if (!formatter) formatter = formatters["XXX"]
  return formatter(x)
}
