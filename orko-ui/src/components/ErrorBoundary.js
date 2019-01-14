import React from "react"

export default class ErrorBoundary extends React.Component {
  constructor(props) {
    super(props)
    this.state = { error: null, errorInfo: null }
  }

  componentDidCatch(error, errorInfo) {
    this.setState({
      error: error,
      errorInfo: errorInfo
    })
  }

  render() {
    if (this.state.errorInfo) {
      const Wrapper = this.props.wrapper
        ? this.props.wrapper
        : ({ message, children }) => (
            <div>
              <h2>{message}</h2>
              {children}
            </div>
          )
      return (
        <Wrapper message="Something went wrong">
          <details style={{ whiteSpace: "pre-wrap" }}>
            {this.state.error && this.state.error.toString()}
            <br />
            {this.state.errorInfo.componentStack}
          </details>
        </Wrapper>
      )
    }
    return this.props.children
  }
}
