import { DEFAULT_KEY, checkCacheValid, generateCacheTTL } from 'redux-cache';
import { Map } from 'immutable';

export const FETCH_TICKER_REQUEST = 'Ticker.FETCH_TICKER_REQUEST';
export const FETCH_TICKER_SUCCESS = 'Ticker.FETCH_TICKER_SUCCESS';
export const FETCH_TICKER_FAILURE = 'Ticker.FETCH_TICKER_FAILURE';

export const fetchTicker = (exchange, counter, base) => (dispatch, getState) => {

    var isCacheValid = checkCacheValid(getState, "ticker-" + exchange + "-" + counter + "-" + base);
	if (isCacheValid) { return null; }

	dispatch({
		type: FETCH_TICKER_REQUEST
    });
    
    const meta = {
        exchange: exchange,
        counter: counter,
        base: base
    };

    fetch(new Request('http://localhost:8080/api/exchanges/' + exchange + '/markets/' + base + "-" + counter + "/ticker", {
        method: 'GET', 
        mode: 'cors', 
        redirect: 'follow',
        credentials: 'include',
        headers: new Headers({
            "Authorization": "Basic YnVsbHk6Ym95",
            "Content-type": "application/json"
        })
    }))
    .then(response => response.json())
    .then(json => {
        dispatch({
            type: FETCH_TICKER_SUCCESS,
            meta: meta,
            payload: json,
        });
    })
    .catch((error) => {
        console.log('error: ', error);
        dispatch({
            type: FETCH_TICKER_FAILURE,
            meta: meta,
            payload: error,
        });
    }); 
};

function tickerName(spec) {
    return spec.exchange + "-" + spec.base + "-" + spec.counter
}

const defaultTicker = {
    ask: 0,
    bid: 0
};

export const initialState = {
    [DEFAULT_KEY]: null,
    tickers: Map(),
    get: function(spec) {
        const ticker = this.tickers ? this.tickers.get(tickerName(spec)) : defaultTicker;
        return ticker ? ticker : defaultTicker;
    }
};

export const reducer = (state = initialState, action) => {

    if (!action || !action.meta)
        return state;

    const emptyResult = () => ({
        ...state,
        [DEFAULT_KEY]: generateCacheTTL()
    });

    switch (action.type) {
        case FETCH_TICKER_SUCCESS:
            if (!action.payload)
                return emptyResult(); 
            return {
                ...state,
                [DEFAULT_KEY]: generateCacheTTL(),
                tickers: state.tickers.set(tickerName(action.meta), action.payload)
            }
        case FETCH_TICKER_FAILURE:
            return emptyResult();
        default:
            return state;
    }
}