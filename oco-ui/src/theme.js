import { keyframes } from "styled-components"
import { lighten } from "polished"

const theme = {
  breakpoints: ["62em"],
  fontSizes: [11, 12, 13, 16],
  fontWeights: {
    normal: 500,
    bold: 700
  },
  colors: {
    black: "#000",
    white: "#fff",
    backgrounds: ["#131722", "#282b38", "#2F3241", "#343747"],
    inputBg: lighten(0.1, "#2F3241"),
    fore: "#aaa",
    emphasis: "#3BB3E4",
    deemphasis: "#555",
    heading: "#fff",
    toolbar: "white",
    boxBorder: "#131722",
    link: "#3BB3E4",

    alert: "#EB4D5C",
    success: "#53B987",

    sell: "#EB4D5C",
    buy: "#53B987",

    inputBorder: "#5C656C"
  },
  radii: [0, 2, 4],
  keyFrames: {
    flashGreen: props => keyframes`
      from {
        background-color: ${props.theme.colors.buy};
        color: black;
      }
    
      to {
        background-color: none;
        color: ${props.theme.colors.fore};
      }
    `,
    flashRed: props => keyframes`
      from {
        background-color ${props.theme.colors.sell};
        color: black;
      }
    
      to {
        background-color: none;
        color: ${props.theme.colors.fore};
      }
    `
  }
}

export default theme
