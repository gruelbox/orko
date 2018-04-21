import { all } from 'redux-saga/effects'

import * as tickerActions from './ticker/actions'

export default function* rootSaga(dispatch, getState) {
  yield all([
    tickerActions.watcher(dispatch, getState)
  ])
}