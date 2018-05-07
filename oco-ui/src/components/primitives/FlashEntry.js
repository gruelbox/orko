import React from "react"
import ReactCSSTransitionGroup from 'react-addons-css-transition-group'

const FlashEntry = ({children, content}) => (
    <ReactCSSTransitionGroup
        transitionName="flashEntry"
        transitionAppear={true}
        transitionAppearTimeout={2000}
        transitionEnter={false}
        transitionLeave={false}>
        {content ? <span>{content}</span> : children}
    </ReactCSSTransitionGroup>
)

export default FlashEntry