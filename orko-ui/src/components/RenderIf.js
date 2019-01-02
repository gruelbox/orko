import React from 'react'

export default class RenderIf extends React.Component {

  shouldComponentUpdate(nextProps) {
    return nextProps.condition === true
  }

  render() {
    return this.props.children
  }
}