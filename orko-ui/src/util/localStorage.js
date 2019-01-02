export function getFromLS(key) {
  let result = getValueFromLS(key)
  if (result === null) return null
  return JSON.parse(result)
}

export function getValueFromLS(key) {
  let ls = null
  if (global.localStorage) {
    try {
      ls = global.localStorage.getItem(key) || null
    } catch (e) {
      /*Ignore*/
    }
  }
  return ls
}

export function saveToLS(key, value) {
  saveValueToLS(key, JSON.stringify(value))
  return value
}

export function saveValueToLS(key, value) {
  if (global.localStorage) {
    global.localStorage.setItem(key, value)
  }
}
