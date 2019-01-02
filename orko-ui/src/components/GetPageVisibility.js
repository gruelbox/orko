import React from 'react'
import PageVisibility from 'react-page-visibility'

export default class GetPageVisibility extends React.Component {
  constructor(props) {
    super(props)
    this.state = {
      visible: true
    };
  }

  handleVisibilityChange = visible => {
    this.setState({ visible });
  }

  render() {
    return (
      <PageVisibility onChange={this.handleVisibilityChange}>
        {this.props.children(this.state.visible)}
      </PageVisibility>
    );
  }
}