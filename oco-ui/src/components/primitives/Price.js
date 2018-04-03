import React from 'react';

import styled from 'styled-components';
import { fontSize, color, fontWeight, space } from 'styled-system';

const PriceRow = styled.tr`
  margin: 0,
  padding: 0
`;

const PriceKey = styled.th.attrs({
  fontSize: 1,
  color: "heading",
  fontWeight: "bold",
  scope: "row",
  pr: 2,
  py: 0,
  m: 0
})`
  text-align: right;
  border-right: 1px solid ${props => props.theme.colors.deemphasis};
  white-space:nowrap;
  ${color}
  ${fontSize}
  ${fontWeight}
  ${space}
`;

const PriceValue = styled.td.attrs({
  fontSize: 1,
  color: "fore",
  py: 0,
  pl: 1,
  m: 0
})`
  cursor: copy;
  &:hover {
    color: ${props => props.theme.colors.link};
  };
  ${color}
  ${fontSize}
  ${fontWeight}
  ${space}
`;

const Price = props => (
  <PriceRow>
    <PriceKey>{props.name}</PriceKey>
    <PriceValue onClick={
      () => {
        if (props.onClick) {
          console.log("Price clicked", props.name, props.children)
          props.onClick(props.children);
        }
      }
    }>
      {props.children}
    </PriceValue>
  </PriceRow>
);

export default Price;