import * as types from './actionTypes';
import authService from '../../services/auth';

export function checkWhiteList() {
  return async(dispatch, getState) => {
    try {
      const result = await authService.checkWhiteList();
      dispatch({ type: types.SET_WHITELIST_STATUS, status: Boolean(result) });
    } catch (error) {
      dispatch({ type: types.SET_WHITELIST_ERROR, error: error.message });
    }
  };
}

export function whitelist(token) {
  return async(dispatch, getState) => {
    try {
      await authService.whitelist(token);
      dispatch({ type: types.SET_WHITELIST_STATUS, status: true });
    } catch (error) {
      dispatch({ type: types.SET_WHITELIST_ERROR, error: error.message });
    }
  };
}

export function defaultLogin(userName, password) {
  return async(dispatch, getState) => {
    await attemptLogin(getState().auth.token.userName, getState().auth.token.password, dispatch);
  };
}

export function login(userName, password) {
  return async(dispatch, getState) => {
    await attemptLogin(userName, password, dispatch);
  };
}

export function logout() {
  return { type: types.SET_LOGGED_OUT };
}

async function attemptLogin(userName, password, dispatch) {
  try {
    const response = await authService.login(userName, password);
    if (response.ok) {
      dispatch({ type: types.SET_LOGIN_SUCCESS, token: {
        userName: userName,
        password: password
      }});
    } else {
      if (response.status === 403) {
        dispatch({ type: types.SET_WHITELIST_EXPIRED });
      } else if (response.status === 401) {
        dispatch({ type: types.SET_LOGIN_FAILED });
      } else {
        dispatch({ type: types.SET_LOGIN_ERROR, error: response.statusText });
      }
    }
  } catch (error) {
    dispatch({ type: types.SET_LOGIN_ERROR, error: error.message });
  }
}

export function handleHttpResponse(response) {
  if (response.status === 403) {
    return { type: types.SET_WHITELIST_EXPIRED };
  } else if (response.status === 401) {
    return { type: types.SET_LOGIN_FAILED };
  }
  return null;
}

export function wrappedRequest(apiRequest, jsonHandler, errorHandler, onSuccess) {
  return async(dispatch, getState) => {
    try {
      const response = await apiRequest(getState().auth);
      if (!response.ok) {
        const authAction = handleHttpResponse(response);
        if (authAction !== null) {
          dispatch(authAction);
        } else {
          throw new Error(response.statusText);
        }
      } else {
        if (onSuccess)
          dispatch(onSuccess());
        if (jsonHandler)
          dispatch(jsonHandler(await response.json()));
      }
    } catch (error) {
      if (errorHandler)
        dispatch(errorHandler(error));
    }
  };
}