export const formatDate = timestamp => {
  var d = new Date(timestamp)
  return d.toLocaleDateString() + " " + d.toLocaleTimeString()
}