import React from "react"
import ReactCSSTransitionGroup from 'react-addons-css-transition-group'

const FadeIn = ({children}) => (
    <ReactCSSTransitionGroup
        transitionName="fadeIn"
        transitionAppear={true}
        transitionAppearTimeout={1000}
        transitionEnter={false}
        transitionLeave={false}>
        {children}
    </ReactCSSTransitionGroup>
)

export default FadeIn