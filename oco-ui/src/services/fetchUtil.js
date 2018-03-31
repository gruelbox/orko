import { Base64 } from 'js-base64';

const defaultSettings = { method: 'GET', mode: 'cors', redirect: 'follow' };

export function get(url, token) {
  return fetch(new Request("/api/" + url, action("GET", token)));
}

export function put(url, token, content) {
  return fetch(new Request("/api/" + url, action("PUT", token, content)));
}

export function del(url, token) {
  return fetch(new Request("/api/" + url, action("DELETE", token)));
}

function action(method, token, content) {
  if (token)
    return {
      ...defaultSettings,
      body: content,
      method: method,
      credentials: 'include',
      headers: new Headers({
        "Authorization": "Basic " + Base64.encode(token.userName + ":" + token.password),
        "Content-type": "application/json"
      })
    }
  else
    return {
      ...defaultSettings,
      method: method
    };
}