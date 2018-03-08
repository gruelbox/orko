import { DEFAULT_KEY, checkCacheValid, generateCacheTTL } from 'redux-cache';
import { Map } from 'immutable';

export const FETCH_PAIRS_SUCCESS = 'Pairs.FETCH_PAIRS_SUCCESS';
export const FETCH_PAIRS_FAILURE = 'Pairs.FETCH_PAIRS_FAILURE';

export const fetchPairs = (exchange) => (dispatch, getState) => {

    if (!getState().auth.valid) {
        console.log("pairs: Not fetching, invalid auth")
        return null;
    }

    var isCacheValid = checkCacheValid(getState, "pairs");
	if (isCacheValid) {
        console.log("pairs: cache hit");
        return null;
    }
    console.log("pairs: cache miss");

    const meta = {
        exchange: exchange,
    };

    return fetch(new Request('http://localhost:8080/api/pairs/' + exchange, {
        method: 'GET', 
        mode: 'cors', 
        redirect: 'follow',
        credentials: 'include',
        headers: getState().auth.headers()
    }))
    .then(response => response.json())
    .then(json => {
        console.log("ticker: got: ", json)
        dispatch({
            type: FETCH_PAIRS_SUCCESS,
            meta: meta,
            payload: json,
        });
    })
    .catch((error) => {
        console.log('error: ', error);
        dispatch({
            type: FETCH_PAIRS_FAILURE,
            meta: meta,
            payload: error,
        });
    }); 
};

export const initialState = {
    [DEFAULT_KEY]: null,
    pairs: []
};

export const reducer = (state = initialState, action) => {

    const defaultResponse = () => ({
        ...state,
        [DEFAULT_KEY]: generateCacheTTL()
    });

    if (!action || !action.meta)
        return defaultResponse();

    switch (action.type) {
        case FETCH_PAIRS_SUCCESS:
            if (!action.payload)
                return defaultResponse(); 
            return {
                ...state,
                [DEFAULT_KEY]: generateCacheTTL(),
                pairs: action.payload
            }
        case FETCH_PAIRS_FAILURE:
            return defaultResponse();
        default:
            return state;
    }
}