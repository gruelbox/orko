export const isValidNumber = val => !isNaN(val) && val !== "" && val !== null

export const formatNumber = (x, scale, undefinedValue) => {
  if (!isValidNumber(x)) return undefinedValue
  const negative = x < 0
  if (scale < 0) {
    const split = negative
      ? (-x).toString().split("-")
      : x.toString().split("-")
    if (split.length > 1) {
      var result = Number(x).toFixed(split[1])
      return negative ? -result : result
    } else {
      return negative ? -split[0] : split[0]
    }
  } else {
    return Number(x).toFixed(scale)
  }
}
