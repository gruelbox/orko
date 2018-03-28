import React from 'react';

const CopyableNumber = props => (
  <span>
    {props.label && (props.label + ": ")}
    <span
      style={{cursor: "copy"}}
      onClick={() => {
        if (props.onClick) props.onClick(props.number);
      }}>
        {props.number}
    </span>
  </span>
);

export default CopyableNumber;