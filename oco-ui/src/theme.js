import { keyframes } from 'styled-components';
import { lighten } from 'polished';

const theme = {
  breakpoints: ['59em'],
  fontSizes: [11, 12, 13, 16],
  fontWeights: {
    normal: 500,
    bold: 700
  },
  colors: {
    black: '#000',
    white: '#fff',
    backgrounds: [
      '#131722',
      '#282b38',
      '#2F3241',
      '#343747'
    ],
    inputBg: lighten(0.1,'#2F3241'),
    fore: '#aaa',
    emphasis: '#26e0dd',
    deemphasis: '#555',
    heading: '#fff',
    toolbar: 'white',
    boxBorder: '#131722',
    link: '#26e0dd',
    alert: 'red',
    success: '#19ff00',
    inputBorder: '#5C656C'
  },
  radii: [ 0, 2, 4 ],
  keyFrames: {
    flashGreen: props => keyframes`
      from {
        text-shadow: 0 0 5px ${props.theme.colors.success};
        color: ${props.theme.colors.success};
      }
    
      to {
        text-shadow: none;
        color: ${props.theme.colors.fore};
      }
    `,
    flashRed: props => keyframes`
      from {
        text-shadow: 0 0 5px ${props.theme.colors.alert};
        color: ${props.theme.colors.alert};
      }
    
      to {
        text-shadow: none;
        color: ${props.theme.colors.fore};
      }
    `
  }
};

export default theme;