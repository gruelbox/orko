import { Base64 } from 'js-base64';

export const SET_CREDENTIALS = 'Auth.SET_CREDENTIALS';
export const WHITELIST = 'Auth.WHITELIST';

const generateHeaders = function(userName, password) {
    return new Headers({
        "Authorization": "Basic " + Base64.encode(userName + ":" + password),
        "Content-type": "application/json"
    });
};

export const clearCredentials = () => (dispatch, getState) => {
    dispatch({
        type: SET_CREDENTIALS,
        meta: initialState
    });
};

export const checkWhitelist = () => (dispatch, getState) => {
    return fetch(new Request('http://localhost:8080/api/auth', {
        method: 'GET', 
        mode: 'cors', 
        redirect: 'follow'
    }))
    .then(response => response.text())
    .then(text => {
        dispatch({
            type: WHITELIST,
            meta: { status: text }
        });
    });
};

export const whitelist = (token) => (dispatch, getState) => {
    return fetch(new Request('http://localhost:8080/api/auth?token=' + token, {
        method: 'PUT', 
        mode: 'cors', 
        redirect: 'follow'
    }))
    .then(response => {
        if (response.status === 200) {
            dispatch({
                type: WHITELIST,
                meta: { status: true }
            });
        }
    });
};

export const setCredentials = (userName, password) => (dispatch, getState) => {
    return fetch(new Request('http://localhost:8080/api/exchanges', {
        method: 'GET', 
        mode: 'cors', 
        redirect: 'follow',
        credentials: 'include',
        headers: generateHeaders(userName, password)
    }))
    .then(response => {
        if (response.status === 200) {
            dispatch({
                type: SET_CREDENTIALS,
                meta: {
                    userName: userName,
                    password: password,
                    valid: true
                }
            });
        } else {
            dispatch({
                type: SET_CREDENTIALS,
                meta: {
                    userName: userName,
                    password: password,
                    valid: false
                }
            });
        }
    });
};

export const initialState = {
    valid: false,
    userName: '',
    password: '',
    whitelisted: false,
    headers: function() {
        return generateHeaders(this.userName, this.password)
    }
};

export const reducer = (state = initialState, action) => {

    if (!action)
        return state;

    switch (action.type) {
        case SET_CREDENTIALS:
            return {
                ...state,
                userName: action.meta.userName,
                password: action.meta.password,
                valid : action.meta.valid
            }
        case WHITELIST:
            return {
                ...state,
                whitelisted: action.meta.status
            }
        default:
            return state;
    }
}