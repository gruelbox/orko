import React from 'react';
import App from './App';
import { Provider } from 'react-redux';
import { configureStore } from './redux/configureStore'

const Root = (props) => {
    const store = configureStore();
    return (
        <Provider store={store}>
            <App />
        </Provider>
    );
}

export default Root;