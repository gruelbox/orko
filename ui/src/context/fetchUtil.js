import { Base64 } from 'js-base64';

const defaultSettings = { method: 'GET', mode: 'cors', redirect: 'follow' };

export function get(url, userName, password) {
  return fetch(new Request("http://localhost:8080/api/" + url, action("GET", userName, password)));
}

export function put(url, userName, password, content) {
  return fetch(new Request("http://localhost:8080/api/" + url, action("PUT", userName, password, content)));
}

function action(method, userName, password, content) {
  if (userName)
    return {
      ...defaultSettings,
      body: content,
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