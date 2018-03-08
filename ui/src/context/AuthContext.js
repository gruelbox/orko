import React from 'react';
import createReactContext from 'create-react-context';
import { put, get } from './fetchUtil';

const AuthContext = createReactContext('auth')

export const AuthConsumer = AuthContext.Consumer;

export class AuthProvider extends React.Component {

  constructor(props) {
    super(props);

    const self = this;

    this.state = {

      valid: false,
      userName: '',
      password: '',
      whitelisted: false,

      clearCredentials: function (userName, password) {
        this.setState({
          userName: '',
          password: '',
        })
      },

      checkWhitelist: function() {
        return get('auth')
          .then(response => response.text())
          .then(text => self.setState({ whitelisted: (text === 'true') }));
      },

      whitelist: function (token) {
        return put('auth?token=' + token)
          .then(response => {
            if (response.status === 200) {
              self.setState({ whitelisted: true });
            } else {
              self.setState({ whitelisted: false });
              throw new Error("Whitelisting failed");
            }
          });
      },

      setCredentials: function(userName, password) {
        return get('exchanges', userName, password)
          .then(response => {
            if (response.status === 200) {
              self.setState({
                userName: userName,
                password: password,
                valid: true
              });
            } else {
              self.setState({
                userName: userName,
                password: password,
                valid: false
              });
              throw new Error("Login failed");
            }
          });
      }

    }
  }

  render() {
    return (
      <AuthContext.Provider value={this.state}>
        {this.props.children}
      </AuthContext.Provider>
    )
  }
}