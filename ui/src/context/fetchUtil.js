import { Base64 } from 'js-base64';

const defaultSettings = { method: 'GET', mode: 'cors', redirect: 'follow' };

export function get(url, userName, password) {
  return fetch(new Request("http://localhost:8080/api/" + url, action("GET", userName, password)));
}

export function put(url, userName, password) {
  return fetch(new Request("http://localhost:8080/api/" + url, action("PUT", userName, password)));
}

function action(method, userName, password) {
  if (userName)
    return {
      ...defaultSettings,
      method: method,
      credentials: 'include',
      headers: new Headers({
        "Authorization": "Basic " + Base64.encode(userName + ":" + password),
        "Content-type": "application/json"
      })
    }
  else
    return {
      ...defaultSettings,
      method: method
    };
}