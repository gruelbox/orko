export const isValidNumber = val => !isNaN(val) && val !== ""

export const formatNumber = (x, scale, undefinedValue) => {
  if (!isValidNumber(x)) return undefinedValue
  if (scale < 0) {
    const split = x.toString().split("-")
    if (split.length > 1) {
      return Number(x).toFixed(split[1])
    } else {
      return split[0]
    }
  } else {
    return Number(x).toFixed(scale)
  }
}
