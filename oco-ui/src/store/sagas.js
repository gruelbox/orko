import { all } from 'redux-saga/effects'

import * as tickerSagas from './ticker/sagas'

export default function* rootSaga(dispatch, getState) {
  yield all([
    tickerSagas.watcher(dispatch, getState),
  ])
}