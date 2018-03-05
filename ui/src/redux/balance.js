import { DEFAULT_KEY, checkCacheValid, generateCacheTTL } from 'redux-cache';
import { Map } from 'immutable';

export const FETCH_BALANCE_REQUEST = 'Balance.FETCH_BALANCE_REQUEST';
export const FETCH_BALANCE_SUCCESS = 'Balance.FETCH_BALANCE_SUCCESS';
export const FETCH_BALANCE_FAILURE = 'Balance.FETCH_BALANCE_FAILURE';

export const fetchBalances = (exchange, currencies) => (dispatch, getState) => {

    var isCacheValid = true;
    for (var currency in currencies) {
        if (!checkCacheValid(getState, "balance-" + exchange + "-" + currency)) {
            isCacheValid = false;
            break;
        }
    }
	if (isCacheValid) { return null; }

	dispatch({
		type: FETCH_BALANCE_REQUEST
    });
    
    const meta = {
        exchange: exchange,
        currencies: currencies
    };

    fetch(new Request('http://localhost:8080/api/exchanges/' + exchange + '/balance/' + currencies.join(","), {
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
            type: FETCH_BALANCE_SUCCESS,
            meta: meta,
            payload: json,
        });
    })
    .catch((error) => {
        console.log('error: ', error);
        dispatch({
            type: FETCH_BALANCE_FAILURE,
            meta: meta,
            payload: error,
        });
    });
};

const zeroBalance = {
    available: 0,
    total: 0
}

export const initialState = {
    [DEFAULT_KEY]: null,
    balances: Map(),
    get: function(exchange, currency) {
        if (!this.balances)
            return zeroBalance;
        const forExchange = this.balances.get(exchange);
        if (!forExchange)
            return zeroBalance;
        const balance = forExchange.get(currency);
        if (!balance)
            return zeroBalance;
        return balance; 
    }
};

export const reducer = (state = initialState, action) => {

    if (!action || !action.meta)
        return state;

    const defaultResponse = () => ({
        ...state,
        [DEFAULT_KEY]: generateCacheTTL(),
    });

    switch (action.type) {
        case FETCH_BALANCE_SUCCESS:
            if (!action.payload)
                return defaultResponse();
            return {
                ...state,
                [DEFAULT_KEY]: generateCacheTTL(),
                balances: state.balances.withMutations(to => {
                    action.meta.currencies.forEach(c => {
                        to.setIn([action.meta.exchange, c], action.payload[c]);
                    }); 
                })
            }
        case FETCH_BALANCE_FAILURE:
            return defaultResponse();
        default:
            return state;
    }
}