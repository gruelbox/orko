import { put, get } from './fetchUtil';
import { Container } from 'unstated';

export default class AuthContainer extends Container {

  state = {
    valid: true,
    userName: 'bully',
    password: 'boy',
    whitelisted: true,
  };

  getUserName = () => this.state.userName;

  getPassword = () => this.state.password;

  isValid = () => this.state.valid;

  clearCredentials = (userName, password) => {
    this.setState({
      userName: '',
      password: '',
      valid: false
    })
  };

  checkWhitelist = () => {
    return get('auth')
      .then(response => response.text())
      .then(text => this.setState({ whitelisted: (text === 'true') }));
  };

  whitelist = (token) => {
    return put('auth?token=' + token)
      .then(response => {
        if (response.status === 200) {
          this.setState({ whitelisted: true });
        } else {
          this.setState({ whitelisted: false });
          throw new Error("Whitelisting failed");
        }
      });
  };

  setCredentials = (userName, password) => {
    return get('exchanges', userName, password)
      .then(response => {
        if (response.status === 200) {
          this.setState({
            userName: userName,
            password: password,
            valid: true
          });
        } else {
          this.setState({
            userName: userName,
            password: password,
            valid: false
          });
          throw new Error("Login failed");
        }
      });
  };
}