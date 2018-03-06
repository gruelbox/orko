import { DEFAULT_KEY, checkCacheValid, generateCacheTTL } from 'redux-cache';
import { Map } from 'immutable';

export const FETCH_TICKER_SUCCESS = 'Ticker.FETCH_TICKER_SUCCESS';
export const FETCH_TICKER_FAILURE = 'Ticker.FETCH_TICKER_FAILURE';

export const fetchTicker = (exchange, counter, base) => (dispatch, getState) => {

    if (!getState().auth.valid)
        return null;

    var isCacheValid = checkCacheValid(getState, "tickers");
	if (isCacheValid) { return null; }

    const meta = {
        exchange: exchange,
        counter: counter,
        base: base
    };

    const headers = getState().auth.headers();
    console.log("ticker: Fetching " + headers);

    fetch(new Request('http://localhost:8080/api/exchanges/' + exchange + '/markets/' + base + "-" + counter + "/ticker", {
        method: 'GET', 
        mode: 'cors', 
        redirect: 'follow',
        credentials: 'include',
        headers: headers
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

    const defaultResponse = () => ({
        ...state,
        [DEFAULT_KEY]: generateCacheTTL()
    });

    if (!action || !action.meta)
        return defaultResponse();

    switch (action.type) {
        case FETCH_TICKER_SUCCESS:
            if (!action.payload)
                return defaultResponse(); 
            return {
                ...state,
                [DEFAULT_KEY]: generateCacheTTL(),
                tickers: state.tickers.set(tickerName(action.meta), action.payload)
            }
        case FETCH_TICKER_FAILURE:
            return defaultResponse();
        default:
            return state;
    }
}