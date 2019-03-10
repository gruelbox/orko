/*
 * Orko
 * Copyright © 2018-2019 Graham Crockford
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
import React, { Component } from "react"
import { Modal, Icon, Form, Button, Message } from "semantic-ui-react"
import FixedModal from "./primitives/FixedModal"
import authService from "../services/auth"
import { setXsrfToken } from "../services/fetchUtil"
import { isValidOtp } from "../util/numberUtils"

export default class Login extends Component {
  constructor(props) {
    super(props)
    this.state = {
      username: "",
      password: "",
      secondFactor: ""
    }
  }

  onChangeUsername = event => {
    this.setState({ username: event.target.value })
  }

  onChangePassword = event => {
    this.setState({ password: event.target.value })
  }

  onChangeSecondFactor = event => {
    this.setState({ secondFactor: event.target.value })
  }

  login = () => {
    authService
      .simpleLogin(this.state)
      .then(({ expiry, xsrf }) => {
        try {
          setXsrfToken(xsrf)
        } catch (error) {
          throw new Error("Malformed access token")
        }
        this.props.onSuccess({
          expiry
        })
      })
      .catch(error => this.props.onError(error.message))
  }

  render() {
    return (
      <FixedModal size="tiny" data-orko="loginModal">
        <Modal.Header>
          <Icon name="lock" />
          Login
        </Modal.Header>
        <Modal.Content>
          <Form error={this.props.error !== null} id="loginForm">
            <Message error header="Error" content={this.props.error} />
            <Form.Field required>
              <label>
                Username{" "}
                <Icon
                  name="question circle"
                  title="Configured on the server in the config file (auth/jwt/userName) or the SIMPLE_AUTH_USERNAME environment variable."
                />
              </label>
              <div className="ui input">
                <input
                  data-orko="username"
                  type="text"
                  placeholder="Enter name"
                  value={this.state.username}
                  onChange={this.onChangeUsername}
                  autoComplete="username"
                />
              </div>
            </Form.Field>
            <Form.Field required>
              <label>
                Password{" "}
                <Icon
                  name="question circle"
                  title="Configured on the server in the config file (auth/jwt/password) or the SIMPLE_AUTH_PASSWORD environment variable."
                />
              </label>
              <div className="ui input">
                <input
                  data-orko="password"
                  type="password"
                  placeholder="Enter password"
                  value={this.state.password}
                  onChange={this.onChangePassword}
                  autoComplete="current-password"
                />
              </div>
            </Form.Field>
            <Form.Field
              error={
                this.state.secondFactor !== "" &&
                !isValidOtp(this.state.secondFactor)
              }
            >
              <label>
                Second factor{" "}
                <Icon
                  name="question circle"
                  title="Optional. Enter a six-digit one-time-password from an authenticator application such as Google Authenticator, removing any spaces. Only required if a secret has been configured on the server in the config file (auth/jwt/secondFactorSecret) or the SIMPLE_AUTH_SECOND_FACTOR environment variable."
                />
              </label>
              <div className="ui input">
                <input
                  data-orko="secondFactor"
                  type="text"
                  placeholder="6 digits, e.g. 123456"
                  value={this.state.secondFactor}
                  onChange={this.onChangeSecondFactor}
                />
              </div>
            </Form.Field>
          </Form>
        </Modal.Content>
        <Modal.Actions>
          <Button
            data-orko="loginSubmit"
            type="submit"
            form="loginForm"
            onClick={this.login}
            disabled={
              this.state.username === "" ||
              this.state.password === "" ||
              (this.state.secondFactor !== "" &&
                !isValidOtp(this.state.secondFactor))
            }
          >
            Login
          </Button>
        </Modal.Actions>
      </FixedModal>
    )
  }
}
