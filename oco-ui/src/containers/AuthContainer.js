import React, { Component } from 'react';
import { connect } from 'react-redux';
import * as actions from '../store/auth/actions';
import { Loader } from 'semantic-ui-react';
import WhitelistingComponent from '../components/WhitelistingComponent';
import CredentialsComponent from '../components/CredentialsComponent';

class AuthContainer extends Component {

  componentDidMount() {
    this.props.dispatch(actions.checkWhiteList());
  }

  onWhitelist = (token) => {
    this.props.dispatch(actions.whitelist(token));
  };

  onLogin = (userName, password) => {
    this.props.dispatch(actions.login(userName, password));
  };

  render() {
    if (this.props.auth.loading) {
      return <Loader active={true}/>;
    } else {
      return (
        <div>
          <WhitelistingComponent
            open={!this.props.auth.whitelisted}
            onApply={this.onWhitelist}
            error={this.props.auth.error}
          />
          <CredentialsComponent
            open={!this.props.auth.loggedIn}
            onApply={this.onLogin}
            error={this.props.auth.error}
          />
        </div>
      );
    }
  } 
}

function mapStateToProps(state) {
  return {
    auth: state.auth
  };
}

export default connect(mapStateToProps)(AuthContainer);