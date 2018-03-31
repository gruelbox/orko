import React from 'react';

import { coinShape, balanceShape, tickerShape } from '../store/coin/reducer';
import PropTypes from 'prop-types';

import { Flex, Box } from 'rebass';

import Chart from './Chart';

import SectionHeading from './primitives/SectionHeading';
import Warning from './primitives/Warning';
import PriceSet from './primitives/PriceSet';
import Price from './primitives/Price';

export const CoinInfoComponent = props => {
  const coin = props.coin;
  if (coin) {
    return (
      <Flex flexWrap='wrap' justifyContent="space-between">
        <Box width={[1, 1]}>
          <SectionHeading>{coin.name}</SectionHeading>
        </Box>
        <Box width={[1/2, 1/4]}>
          {(!props.balance)
            ? <Warning>Cannot fetch balance</Warning>
            : <PriceSet>
                <Price name={coin.base + " total"} onClick={props.onClickNumber}>{props.balance[coin.base].total}</Price>
                <Price name={coin.counter + " total"} onClick={props.onClickNumber}>{props.balance[coin.counter].total}</Price>
              </PriceSet>
          }
        </Box>
        <Box width={[1/2, 1/4]}>
          {(!props.balance)
            ? <Warning>Cannot fetch balance</Warning>
            : <PriceSet>
                <Price name={coin.base + " available"} onClick={props.onClickNumber}>{props.balance[coin.base].available}</Price>
                <Price name={coin.counter + " available"} onClick={props.onClickNumber}>{props.balance[coin.counter].available}</Price>
              </PriceSet>
          }
        </Box>
        <Box width={[1/2, 1/4]}>
          {(!props.ticker)
            ? <Warning>Cannot fetch ticker</Warning>
            : <PriceSet>
                <Price name="Bid" onClick={props.onClickNumber}>{props.ticker.bid}</Price>
                <Price name="Last" onClick={props.onClickNumber}>{props.ticker.last}</Price>
                <Price name="Ask" onClick={props.onClickNumber}>{props.ticker.ask}</Price>
              </PriceSet>
          }
        </Box>
        <Box width={[1/2, 1/4]}>
          {(!props.ticker)
            ? <Warning>Cannot fetch ticker</Warning>
            : <PriceSet>
                <Price name="High" onClick={props.onClickNumber}>{props.ticker.high}</Price>
                <Price name="Open" onClick={props.onClickNumber}>{props.ticker.open}</Price>
                <Price name="Low" onClick={props.onClickNumber}>{props.ticker.low}</Price>
              </PriceSet>
          }
        </Box>
        <Box width={[1, 1]}>
          <div>
            <Chart coin={coin}/>
          </div>
        </Box>
      </Flex>
    );
  } else {
    return <div>No coin selected</div>;
  }
};

export default CoinInfoComponent;

CoinInfoComponent.propTypes = {
  coin: PropTypes.shape(coinShape),
  balance: PropTypes.shape(balanceShape),
  ticker: PropTypes.shape(tickerShape),
  onToggleChart: PropTypes.func,
  onRemove: PropTypes.func,
  onClickNumber: PropTypes.func
};