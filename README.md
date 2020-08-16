# Orko [![Twitter](http://i.imgur.com/wWzX9uB.png)](https://twitter.com/gruelbox)
<!-- ALL-CONTRIBUTORS-BADGE:START - Do not remove or modify this section -->
[![All Contributors](https://img.shields.io/badge/all_contributors-5-orange.svg?style=flat-square)](#contributors-)
<!-- ALL-CONTRIBUTORS-BADGE:END -->
[![Collaborate on Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/gruelbox/community)
[![Current version](https://img.shields.io/github/release/gruelbox/orko.svg)](https://github.com/gruelbox/orko/releases/latest)

[![CircleCI](https://circleci.com/gh/gruelbox/orko/tree/master.svg?style=svg&circle-token=3e040c3e064daf7408b29df31c61af9c73ea862a)](https://circleci.com/gh/gruelbox/orko/tree/master)
[![Sonarcloud Security Rating](https://sonarcloud.io/api/project_badges/measure?project=com.gruelbox%3Aorko-parent&metric=security_rating)](https://sonarcloud.io/dashboard?id=com.gruelbox%3Aorko-parent)
[![Sonarcloud Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=com.gruelbox%3Aorko-parent&metric=vulnerabilities)](https://sonarcloud.io/dashboard?id=com.gruelbox%3Aorko-parent)
[![Sonarcloud Coverage](https://sonarcloud.io/api/project_badges/measure?project=com.gruelbox%3Aorko-parent&metric=coverage)](https://sonarcloud.io/dashboard?id=com.gruelbox%3Aorko-parent)
[![CodeFactor](https://www.codefactor.io/repository/github/gruelbox/orko/badge)](https://www.codefactor.io/repository/github/gruelbox/orko)

Fed up of logging into multiple separate cryptocurrency exchanges? Frustrated by missing features? Want to use advanced order types such as trailing stops on exchanges that don't support them?

Orko is a **self hosted** web application which provides a unified dashboard to control numerous cryptocurrency exchanges, allowing you to trade and manage your portfolio, even if it is spread across multiple exchanges, all from one screen. It works seamlessly on desktop and mobile, so you can have the same trading experience wherever you go.

All this without sharing your API keys with anyone.

![Screenshot](https://github.com/gruelbox/orko/blob/master/.github/app1.PNG?raw=true)

Feel like creating your own special order types, such as soft stops, stops triggered off the price of a different asset, or a "Nuke" button which sells all your alt positions? Orko features a [scripting](https://github.com/gruelbox/orko/wiki/Scripting) API allowing you to design your own bespoke order types.

![Scripting UI](https://github.com/gruelbox/orko/blob/master/.github/scripting1.PNG?raw=true)

Best of all, it's completely [free](https://www.fsf.org/about/what-is-free-software) (as in "free speech" as well as "free beer"). Read about some more [things it can do](https://github.com/gruelbox/orko/wiki/Example-Use-Cases).

<img align="right" src="https://github.com/gruelbox/orko/blob/master/.github/mobile1.png?raw=true" width="281" height="609"/>

## Status

**Beta**. Please read the [project status page](https://github.com/gruelbox/orko/wiki/Project-status). Particularly, please note that the application has only so far been tested on **Chrome desktop** and **Chrome mobile**.

Orko is a new application which has missing features and some well-known bugs. It needs your help. [Read more](https://github.com/gruelbox/orko/wiki/Why-Orko) about Orko, [why it's free](https://github.com/gruelbox/orko/wiki/Supporting_The_Project) and [how to help](https://github.com/gruelbox/orko/wiki/Project-status).

## Installation

[**Windows**](https://github.com/gruelbox/orko/wiki/Local-installation#on-windows) | [**Ubuntu/Debian**](https://github.com/gruelbox/orko/wiki/Local-installation#on-ubuntudebian) | [**Docker**](https://github.com/gruelbox/orko/wiki/Installing-using-Docker) | [**Heroku quick**](https://github.com/gruelbox/orko/wiki/One-click-installation-on-Heroku) | [**Heroku manual**](https://github.com/gruelbox/orko/wiki/Manual-installation-on-Heroku)

[![Deploy to Heroku](https://www.herokucdn.com/deploy/button.svg)](https://heroku.com/deploy?template=https://github.com/gruelbox/orko/tree/stable)

## Demo

- **Address:** https://orko-demo.herokuapp.com/
- **Username:** trader1
- **Password:** givemeshitcoins
- **Second factor:** Leave blank

Paper trading only. Can take 30 seconds to spin up when accessed. Don't expect the full experience.

## Exchange Support

### General

Orko does not support the following on any exchange: **deposits**, **withdrawals**, **funding** or **account management**. It is purely for trading. [Margin trading](https://github.com/gruelbox/orko/issues/83) including position management is on the roadmap but not supported yet.

Exchanges with "excellent" support use **streaming updates**, which means that generally most changes will appear in the UI almost immediately. Without streaming updates, changes can take a number of seconds to be reflected in the UI. Usually, streaming updates are possible for any exchange, but involve considerably more work to implement, so exchanges tend to be added first without streaming updates and get updated later.

### By Exchange

| Exchange     | Support level     | Missing trading features                                                             | Notes                                                            |
| ------------ | ----------------- | ------------------------------------------------------------------------------------ | ---------------------------------------------------------------- |
| Binance      | ✔️ Excellent      | Dust conversion                                                                      | Almost perfect.                                                  |
| Bitfinex     | ✔️ Excellent      | True OCO trades, post only, reduce only, visibility of trades and positions on chart | Short on features, but what is supported is supported perfectly. |
| Coinbase Pro | ✔️ Excellent      | Post only, good-till-cancelled                                                       | Very good, with one or two minor known issues.                   |
| Kucoin       | ✔️ Good           | Streaming updates                                                                    | Works well other than the lack of streaming updates.             |
| Bittrex      | ✔️ Good           | Streaming updates, stop orders                                                       | Works well other than the lack of streaming updates.             |
| Bitmex       | ⚠️ In development | Streaming updates, most complex order types, balances, historical trades             | Not recommended for serious use yet.                             |
| Kraken       | ⚠️ In development | Streaming updates, stop orders                                                       | Read-only.                                                       |

## Help wanted

At the moment this project is a labour of love for just me, I am creating new bug and enhancement issues faster than I am closing them, and I [need help](https://github.com/gruelbox/orko/issues/111)!

The **back-end server** is written in Java and is based on [XChange](https://github.com/knowm/XChange) and [xstream-stream](https://github.com/bitrich-info/xchange-stream), to which the project is a significant contributor. Please consider helping these great projects - it has a knock-on effect on Orko.

The **front-end UI** (mobile and desktop) is written in Javascript and is based on React+Redux.

## Everything else

See [the wiki](https://github.com/gruelbox/orko/wiki)!

## Contributors ✨

Thanks goes to these wonderful people ([emoji key](https://allcontributors.org/docs/en/emoji-key)):

<!-- ALL-CONTRIBUTORS-LIST:START - Do not remove or modify this section -->
<!-- prettier-ignore-start -->
<!-- markdownlint-disable -->
<table>
  <tr>
    <td align="center"><a href="https://github.com/badgerwithagun"><img src="https://avatars0.githubusercontent.com/u/6483013?v=4" width="100px;" alt=""/><br /><sub><b>Graham Crockford</b></sub></a><br /><a href="https://github.com/gruelbox/orko/commits?author=badgerwithagun" title="Code">💻</a> <a href="#content-badgerwithagun" title="Content">🖋</a> <a href="https://github.com/gruelbox/orko/commits?author=badgerwithagun" title="Documentation">📖</a> <a href="#design-badgerwithagun" title="Design">🎨</a> <a href="#infra-badgerwithagun" title="Infrastructure (Hosting, Build-Tools, etc)">🚇</a> <a href="#maintenance-badgerwithagun" title="Maintenance">🚧</a> <a href="#security-badgerwithagun" title="Security">🛡️</a> <a href="https://github.com/gruelbox/orko/commits?author=badgerwithagun" title="Tests">⚠️</a> <a href="#userTesting-badgerwithagun" title="User Testing">📓</a></td>
    <td align="center"><a href="https://github.com/lazrfocus"><img src="https://avatars3.githubusercontent.com/u/52254582?v=4" width="100px;" alt=""/><br /><sub><b>lazrfocus</b></sub></a><br /><a href="#ideas-lazrfocus" title="Ideas, Planning, & Feedback">🤔</a></td>
    <td align="center"><a href="https://www.lowwor.com/"><img src="https://avatars3.githubusercontent.com/u/5444033?v=4" width="100px;" alt=""/><br /><sub><b>lowwor</b></sub></a><br /><a href="https://github.com/gruelbox/orko/commits?author=lowwor" title="Code">💻</a> <a href="#userTesting-lowwor" title="User Testing">📓</a></td>
    <td align="center"><a href="https://github.com/longtran84"><img src="https://avatars3.githubusercontent.com/u/33080326?v=4" width="100px;" alt=""/><br /><sub><b>longtran84</b></sub></a><br /><a href="https://github.com/gruelbox/orko/issues?q=author%3Alongtran84" title="Bug reports">🐛</a></td>
    <td align="center"><a href="https://github.com/AwooOOoo"><img src="https://avatars2.githubusercontent.com/u/17175274?v=4" width="100px;" alt=""/><br /><sub><b>Paul Matthews</b></sub></a><br /><a href="https://github.com/gruelbox/orko/commits?author=AwooOOoo" title="Code">💻</a> <a href="https://github.com/gruelbox/orko/issues?q=author%3AAwooOOoo" title="Bug reports">🐛</a></td>
  </tr>
</table>

<!-- markdownlint-enable -->
<!-- prettier-ignore-end -->
<!-- ALL-CONTRIBUTORS-LIST:END -->

This project follows the [all-contributors](https://github.com/all-contributors/all-contributors) specification. Contributions of any kind welcome!
