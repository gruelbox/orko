import { all } from 'redux-saga/effects'

import * as tickerActions from './ticker/actions'

export default function* rootSaga() {
  yield all([
    tickerActions.watcher()
  ])
}