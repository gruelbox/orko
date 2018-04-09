import React from 'react';
import styled from 'styled-components';

const PriceTable = styled.table`
  margin: 0;
  padding: 0;
  margin-left: auto;
  margin-right: 0;
  margin-bottom: 0;
`;

const PriceSet = props => (
  <PriceTable summary={props.summary}>
    <caption style={{display: 'none'}}>{props.caption}</caption>
    <thead style={{display: 'none'}}>
        <tr><th scope="col">Key</th><th scope="col">Value</th></tr>
    </thead>
    <tbody>  
      {props.children}
    </tbody>
  </PriceTable>
)

export default PriceSet;