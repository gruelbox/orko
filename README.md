# Orko [![Twitter](http://i.imgur.com/wWzX9uB.png)](https://twitter.com/orkotrading)

[![Collaborate on Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/gruelbox/community)	
[![CircleCI](https://circleci.com/gh/gruelbox/orko/tree/master.svg?style=svg&circle-token=3e040c3e064daf7408b29df31c61af9c73ea862a)](https://circleci.com/gh/gruelbox/orko/tree/master)
[![Cypress.io tests](https://img.shields.io/badge/cypress.io-tests-green.svg?style=flat-square)](https://dashboard.cypress.io/#/projects/ttud56/runs)
[![Travis](https://travis-ci.org/gruelbox/orko.svg?branch=master)](https://travis-ci.org/gruelbox/orko)
[![Sonarcloud Security Rating](https://sonarcloud.io/api/project_badges/measure?project=com.gruelbox%3Aorko-parent&metric=security_rating)](https://sonarcloud.io/dashboard?id=com.gruelbox%3Aorko-parent)
[![Sonarcloud Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=com.gruelbox%3Aorko-parent&metric=vulnerabilities)](https://sonarcloud.io/dashboard?id=com.gruelbox%3Aorko-parent)
[![Sonarcloud Coverage](https://sonarcloud.io/api/project_badges/measure?project=com.gruelbox%3Aorko-parent&metric=coverage)](https://sonarcloud.io/dashboard?id=com.gruelbox%3Aorko-parent)

[![Deploy to Heroku](https://www.herokucdn.com/deploy/button.svg)](https://heroku.com/deploy?template=https://github.com/gruelbox/orko/tree/stable)

Are you fed up of logging into multiple separate cryptocurrency exchanges to trade and track your portfolio? Frustrated at the different feature levels exchanges provide? Wanting to use advanced order types on exchanges that don't support them, such as trailing stops or one-cancels-other?

Orko is a **self hosted** web application which provides a unified dashboard to control numerous cryptocurrency exchanges, allowing you to trade and manage your portfolio, even if it is spread across multiple exchanges, all from one screen. It works seamlessly on desktop and mobile, so you can have the same trading experience wherever you go.

All this without sharing your API keys with anyone.

![Screenshot](.github/app1.PNG)

Feel like creating your own special order types, such as soft stops which ignore sharp wicks or stops triggered off the price of a different asset? Orko features a [scripting](https://github.com/gruelbox/orko/wiki/Scripting) API allowing you to design your own bespoke order types.

![Scripting UI](.github/scripting1.PNG)

And best of all, it's completely [free](https://www.fsf.org/about/what-is-free-software) (as in "free speech" as well as "free beer").

Read about some more [things it can do](https://github.com/gruelbox/orko/wiki/Example-Use-Cases).

<img align="right" src=".github/mobile1.png" width="281" height="609"/>

## Status

**Beta**. Please read the [project status page](https://github.com/gruelbox/orko/wiki/Project-status).

Particularly, please note that the application has only so far been tested on Chrome desktop and Chrome mobile.

Orko is a new application which has missing features and some well-known bugs. It needs your help. [Read more](https://github.com/gruelbox/orko/wiki/Why-Orko) about Orko, [why it's free](https://github.com/gruelbox/orko/wiki/Supporting_The_Project) and [how to help](https://github.com/gruelbox/orko/wiki/Project-status).

## Demo site

Note that this uses very limited and not particularly sophisticated **paper trading**, which doesn't work fantastically, but it allows you to get an idea of how it all works.  It's also a shared environment, so could easily be broken, and finally it takes 15-30 second to spin up on first use.  With all that out of the way:

- **Address:** https://orko-demo.herokuapp.com/
- **Username:** trader1
- **Password:** givemeshitcoins
- **Second factor:** Leave blank

## Quick-start Installation

- [On a local machine](https://github.com/gruelbox/orko/wiki/Local-installation)
- [One-click install on Heroku](https://github.com/gruelbox/orko/wiki/One-click-installation-on-Heroku)
- [Manual install on Heroku](https://github.com/gruelbox/orko/wiki/Manual-installation-on-Heroku)

## Exchange Support

| Exchange | Support level | Missing features |Notes |
| -------- | ------------- | ---- | ----- |
| Binance  | Near perfect  | Deposits, withdrawals, dust conversion, account management | Mostly as responsive, or more so, than the Binance website at trading. In particular, it is much more responsive during periods of high load, such as during a pump on BTC. |
| Bitfinex | Excellent     | Deposits, withdrawals, [margin trading](https://github.com/gruelbox/orko/issues/83), funding, true OCO trades, post only, reduce only, visibility of trades and positions on chart, account management | Similar quality of experience to Binance, but more obviously short on features compared to Bitfinex's own site. |
| Coinbase Pro | Excellent | Deposits, withdrawals, post only, good-till, account management | Again, very good user experience, but with a few small UI glitches. |
| Bittrex | Working | **Streaming**, deposits, withdrawals, stop orders, account management | Perfectly working for low frequency trading but not as smooth an experience. |
| Kraken | Minimal | **Streaming**, margin trading, deposits, withdrawals, stop orders, account management | As with Kucoin, with a few specific high-profile issues needing resolving. |
| Bitmex | Minimal | **Streaming**, deposits, withdrawals, account management, most complex order types, position management, leverage setting, balances, historical trades | Bare minimum for placing and cancelling simple limit trades and stops at your currently selected leverage. This has only just started to be implemented. |
| Kucoin | *Broken* | Everything except ticker, order book and market trades | Kucoin platform 2.0 has broken this. It's currently being fixed (see https://github.com/knowm/XChange/issues/2914) |

* **Streaming** = Websocket streaming updates. Most operations such as trades occur immediately but can take a short while to show in the UI. Without streaming, exchange data is fetched periodically instead. This is quick to implement in Orko but provides a less fluid user experience.  It is usually the first phase in implementing an exchange.

## Help wanted

At the moment this project is a labour of love for just me, I am creating new bug and enhancement issues faster than I am closing them, and I [need help](https://github.com/gruelbox/orko/issues/111)!

The **back-end server** is written in Java and is based on [XChange](https://github.com/knowm/XChange) and [xstream-stream](https://github.com/bitrich-info/xchange-stream), to which the project is a significant contributor. Please consider helping these great projects - it has a knock-on effect on Orko.

The **front-end UI** (mobile and desktop) is written in Javascript and is based on React+Redux.

## Everything else

See [the wiki](https://github.com/gruelbox/orko/wiki)!
