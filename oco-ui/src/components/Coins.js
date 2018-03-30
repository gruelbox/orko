import React from 'react';
import { Link } from 'react-router-dom';

import MidComponentBox from './primitives/MidComponentBox';

const Coins = props => (
  <MidComponentBox>
    {props.coins.map(coin => (
      <li><a onClick={() => props.onClick(coin)} style={{cursor: "pointer"}}>{coin.name}</a></li>
    ))}
  </MidComponentBox>
);

export default Coins;