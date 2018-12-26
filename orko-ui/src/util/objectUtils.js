export function areEqualShallow(a, b) {
  for (var key in a) {
    if (!(key in b) || a[key] !== b[key]) {
      return false
    }
  }
  for (key in b) {
    if (!(key in a) || a[key] !== b[key]) {
      return false
    }
  }
  return true
}

export function replaceInArray(arr, replacement, find) {
  var result = []
  var found = false
  for (let o of arr) {
    if (find(o)) {
      result.push(replacement)
      found = true
    } else {
      result.push(o)
    }
  }
  if (!found) result.push(replacement)
  return result
}
