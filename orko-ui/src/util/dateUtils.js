export const formatDate = timestamp => {
  var d = new Date(timestamp)
  return d.toLocaleDateString() + " " + d.toLocaleTimeString()
}

export const unixToDate = timestamp => {
  return new Date(timestamp * 1000)
}
