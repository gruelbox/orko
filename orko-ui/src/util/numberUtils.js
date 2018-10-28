export const isValidNumber = val => !isNaN(val) && val !== ""

export const formatNumber = (x, scale, undefinedValue) => {
  if (!isValidNumber(x)) return undefinedValue
  if (scale < 0) {
    return Number(x).toString()
  } else {
    return Number(x).toFixed(scale)
  }
}
