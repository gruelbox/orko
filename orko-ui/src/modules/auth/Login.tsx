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
import React, { useState, FormEvent } from "react"
import { Modal, Icon, Form, Button, Message } from "semantic-ui-react"
import { isValidOtp } from "modules/common/util/numberUtils"
import LoginDetails from "./LoginDetails"

interface LoginProps {
  error: string
  onLogin(details: LoginDetails): void
}

const Login: React.FC<LoginProps> = (props: LoginProps) => {
  const [username, setUserName] = useState("")
  const [password, setPassword] = useState("")
  const [secondFactor, setSecondFactor] = useState("")

  const onChangeUsername = (event: FormEvent<HTMLInputElement>) =>
    setUserName(event.currentTarget.value)
  const onChangePassword = (event: FormEvent<HTMLInputElement>) =>
    setPassword(event.currentTarget.value)
  const onChangeSecondFactor = (event: FormEvent<HTMLInputElement>) =>
    setSecondFactor(event.currentTarget.value)

  return (
    <Modal open={true} size="tiny" data-orko="loginModal">
      <Modal.Header>
        <Icon name="lock" />
        Login
      </Modal.Header>
      <Modal.Content>
        <Form error={props.error !== null} id="loginForm">
          <Message error header="Error" content={props.error} />
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
                value={username}
                onChange={onChangeUsername}
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
                value={password}
                onChange={onChangePassword}
                autoComplete="current-password"
              />
            </div>
          </Form.Field>
          <Form.Field error={secondFactor !== "" && !isValidOtp(secondFactor)}>
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
                value={secondFactor}
                onChange={onChangeSecondFactor}
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
          onClick={() => props.onLogin({ username, password, secondFactor })}
          disabled={
            username === "" ||
            password === "" ||
            (secondFactor !== "" && !isValidOtp(secondFactor))
          }
        >
          Login
        </Button>
      </Modal.Actions>
    </Modal>
  )
}

export default Login
