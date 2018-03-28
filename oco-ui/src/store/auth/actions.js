import * as types from './actionTypes';
import authService from '../../services/auth';

export function checkWhiteList() {
  return async(dispatch, getState) => {
    try {
      const result = await authService.checkWhiteList();
      dispatch({ type: types.UPDATE, payload: {
        whitelisted: result,
        error: null
      }});
    } catch (error) {
      dispatch({ type: types.UPDATE, payload: {
        whitelisted: false,
        error: error.message
      }});
    }
  };
}

export function whitelist(token) {
  return async(dispatch, getState) => {
    try {
      await authService.whitelist(token);
      dispatch({ type: types.UPDATE, payload: {
        whitelisted: true,
        error: null
      }});
    } catch (error) {
      dispatch({ type: types.UPDATE, payload: {
        error: error.message
      }});
    }
  };
}

export function login(userName, password) {
  return async(dispatch, getState) => {
    try {
      const response = await authService.login(userName, password);
      if (!response.ok) {
        if (response.status === 403) {
          dispatch({ type: types.UPDATE, payload: {
            userName: userName,
            password: password,
            whitelisted: false,
            loggedIn: false,
            error: "Whitelisting expired"
          }});
        } else if (response.status === 401) {
          dispatch({ type: types.UPDATE, payload: {
            userName: userName,
            password: password,
            loggedIn: false,
            error: getState().loading ? null : "Invalid username/password"
          }});
        } else {
          dispatch({ type: types.UPDATE, payload: {
            userName: userName,
            password: password,
            loggedIn: false,
            error: getState().loading ? null : response.statusText
          }});
        }
      } else {
        dispatch({ type: types.UPDATE, payload: {
          userName: userName,
          password: password,
          loggedIn: true,
          error: null
        }});
      }
    } catch (error) {
      dispatch({ type: types.UPDATE, payload: {
        userName: userName,
        password: password,
        loggedIn: false,
        error: error.message
      }});
    }
  };
}

export function handleHttpResponse(response) {
  if (response.status === 403) {
    return {
      whitelisted: false,
      error: "Whitelisting expired"
    };
  } else if (response.status === 401) {
    return {
      loggedIn: false,
      error: "Invalid username/password"
    };
  }
  return null;
}