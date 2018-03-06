import { rootReducer, initialState } from './reducers'
import { compose, createStore, applyMiddleware } from 'redux';
import { cacheEnhancer } from "redux-cache";
import thunk from "redux-thunk";

const middleware = [thunk];

export const configureStore = () => {
  return createStore(
    rootReducer,
    initialState,
    compose(
      applyMiddleware(...middleware),
		  cacheEnhancer({ log: false }),
    )
  );
}