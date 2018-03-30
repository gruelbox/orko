import { put, get, del } from './fetchUtil';
import React, { Component } from 'react';
import createReactContext from 'create-react-context';
import { Loader } from 'semantic-ui-react';
import Whitelisting from './Whitelisting';
import Credentials from './Credentials';

const AuthContext = createReactContext('auth');

export const AuthConsumer = AuthContext.Consumer;

export class AuthProvider extends Component {

  constructor(props) {
    super(props);
    this.state = {
      self: this,
      loading: true,
      valid: false,
      userName: 'bully',
      password: 'boys',
      whitelisted: false,
      error: undefined,
      parseToJson: function(response) {
        if (response.ok) {
          return response.json();
        } else {
          this.parseResponse(response);
          throw new Error(response.statusText);
        }
      },
      parseResponse: function(response) {
        if (response.status === 403) {
          this.setState({
            valid: false,
            whitelisted: false,
            loading: false,
            error: "Whitelisting expired"
          })
          return true;
        } else if (response.status === 401) {
          this.setState({
            valid: false,
            whitelisted: true,
            loading: false,
            error: "Invalid username/password"
          })
          return true;
        }
        return false;
      },
      signout: function() {
        del('auth');
        this.setState({
          userName: undefined,
          password: undefined,
          valid: false,
          whitelisted: false,
          error: "Signed out"
        });
      },
      invalidate: () => {
        del('auth');
      }
    };
  }

  componentDidMount() {
    if (!this.state.valid || !this.state.whitelisted)
      this.onLogin(this.state.userName, this.state.password);
  }

  onWhitelist = (token) => {
    return put('auth?token=' + token)
      .then(response => {
        if (response.status === 200) {
          this.setState({
            whitelisted: true,
            error: null
          });
        } else {
          throw new Error("Whitelisting failed (" + response.status + ")");
        }
      })
      .catch(error => {
        this.setState({ error: error.message });
      });
  };

  onLogin = (userName, password) => {
    this.setState({
      userName: userName,
      password: password
    });
    return get('exchanges', userName, password)
      .then(response => {
        if (response.status === 200) {
          this.setState({
            valid: true,
            whitelisted: true,
            loading: false,
            error: null
          });
        } else if (!this.state.parseResponse(response)) {
          throw new Error("Login failed (" + response.status + ")");
        }
      })
      .catch(error => {
        this.setState({
          valid: false,
          error: error.message,
          loading: false
        });
      });
  };

  render() {
    if (this.state.loading) {
      return <Loader active={true}/>;
    } else {
      return (
        <AuthContext.Provider value={this.state}>
          {this.props.children}
          <Whitelisting open={!this.state.whitelisted} onApply={this.onWhitelist} error={this.state.error} />
          <Credentials open={this.state.whitelisted && !this.state.valid} onApply={this.onLogin} error={this.state.error} />
        </AuthContext.Provider>
      );
    }
  }
}