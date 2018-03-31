import React from 'react';
import ForeSpan from './ForeSpan';

const CopyableNumber = props => (
  <span>
    <ForeSpan color="heading">
      {props.label && (props.label + ": ")}
    </ForeSpan>
    <ForeSpan
      style={{cursor: "copy"}}
      onClick={() => {
        if (props.onClick) props.onClick(props.number);
      }}>
        {props.number}
    </ForeSpan>
  </span>
);

export default CopyableNumber;