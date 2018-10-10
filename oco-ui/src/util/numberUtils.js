export const isValidNumber = val => !isNaN(val) && val !== ""

export const formatNumber = (x, scale, undefinedValue) => {
  if (!isValidNumber(x)) return undefinedValue
  return Number(x).toFixed(scale)
}
