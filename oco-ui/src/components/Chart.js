import React, { Component } from 'react';
import Resizable from 're-resizable';
import styled from 'styled-components';

const SCRIPT_ID = 'tradingview-widget-script';
const CONTAINER_ID = 'tradingview-widget-container';

export default class Chart extends Component {

  shouldComponentUpdate(nextProps, nextState, nextContext) {
    return this.props.coin.key !== nextProps.coin.key;
  }

  componentDidMount = () => this.appendScript(this.initWidget);

  appendScript = (onload) => {
    if (this.scriptExists()) {
      /* global TradingView */
      if (typeof TradingView === 'undefined') {
        this.updateOnloadListener(onload);
        return;
      }
      onload();
      return;
    }
    const script = document.createElement('script');
    script.id = SCRIPT_ID;
    script.type = 'text/javascript';
    script.async = true;
    script.src = 'https://s3.tradingview.com/tv.js';
    script.onload = onload;
    document.getElementsByTagName('head')[0].appendChild(script);
  };

  getScriptElement = () =>
    document.getElementById(SCRIPT_ID);

  scriptExists = () =>
    this.getScriptElement() !== null;

  updateOnloadListener = (onload) => {
    const script = this.getScriptElement();
    const oldOnload = script.onload;
    return script.onload = () => {
      oldOnload();
      onload();
    };
  };

  componentDidUpdate = () => {
    document.getElementById(this.containerId).innerHTML = '';
    this.initWidget();
  };

  initWidget = () => {
    new TradingView.widget({
      "autosize": true,
      "symbol": this.symbol(),
      "interval": "240",
      "timezone": "UTC",
      "theme": "Dark",
      "style": "1",
      "locale": "en",
      "toolbar_bg": "#f1f3f6",
      "enable_publishing": false,
      "withdateranges": true,
      "hide_side_toolbar": false,
      "save_image": false,
      "show_popup_button": true,
      "popup_width": "1000",
      "popup_height": "650",
      "container_id": CONTAINER_ID
    })
  };

  symbol = () => {
    var exchange = this.props.coin.exchange.toUpperCase();
    if (exchange === 'GDAX') {
      exchange = "COINBASE";
    }
    return exchange + ":" + this.props.coin.base + this.props.coin.counter;
  }

  render() {

    const ChartContainer = styled.div`
      overflow: hidden;
    `;

    const ChartInner = styled.div`
      margin-top: -1px;
      margin-left: -8px;
      margin-right: -8px;
      margin-bottom: -8px;
    `;

    return (
      <ChartContainer>
        <ChartInner>
          <Resizable
            defaultSize={{width: "100%", height: 400}}
            enable={{
              top:false,
              right:false,
              bottom:true,
              left:false,
              topRight:false,
              bottomRight:false,
              bottomLeft:false,
              topLeft:false
            }}
          >
            <div style={{height: "100%"}} id={CONTAINER_ID}/>
          </Resizable>
        </ChartInner>
      </ChartContainer>
    );
  }
}