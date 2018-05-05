import React from "react"
import ReactDOM from "react-dom"
import "./index.css"
import "semantic-ui-css/semantic.min.css"
import "./react-table.css"
import App from "./App"
import { injectGlobal } from "styled-components"

const globalCss = `
    .flashEntry-appear {
        color: white;
        text-shadow: 0px 0px 5px white;
    }
      
    .flashEntry-appear.flashEntry-appear-active {
        color: #aaa;
        text-shadow: none;
        transition: color 2s, text-shadow 2s;
    }

    .fadeIn-appear {
        opacity: 0.01;
    }
      
    .fadeIn-appear.example-appear-active {
        opacity: 1;
        transition: opacity .5s ease-in;
    }
`

injectGlobal`${globalCss}`

ReactDOM.render(<App />, document.getElementById("root"))
