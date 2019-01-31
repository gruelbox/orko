# Orko [![Twitter](http://i.imgur.com/wWzX9uB.png)](https://twitter.com/orkotrading)

[![CircleCI](https://circleci.com/gh/gruelbox/orko/tree/master.svg?style=svg&circle-token=3e040c3e064daf7408b29df31c61af9c73ea862a)](https://circleci.com/gh/gruelbox/orko/tree/master)
[![Cypress.io tests](https://img.shields.io/badge/cypress.io-tests-green.svg?style=flat-square)](https://cypress.io)

[![Deploy to Heroku](https://www.herokucdn.com/deploy/button.svg)](https://heroku.com/deploy?template=https://github.com/gruelbox/orko)

Are you up of logging into multiple separate cryptocurrency exchanges to trade and track your portfolio? Frustrated at the different feature levels exchanges provide? Wanting to use advanced order types on exchanges that don't support them, such as trailing stops or one-cancels-other?

Orko is a **self hosted** web application which provides a unified dashboard to control numerous cryptocurrency exchanges, allowing you to trade and manage your portfolio, even if it is spread across multiple exchanges, all from one screen. It works seamlessly on desktop and mobile, so you can have the same trading experience wherever you go.

All this without sharing your API keys with anyone.  

![Screenshot](.github/app1.PNG)

Feel like creating your own special order types, such as soft stops which ignore sharp wicks or stops triggered off the price of a different asset?  Orko features a [scripting](https://github.com/gruelbox/orko/wiki/Scripting) API allowing you to design your own bespoke order types.

![Scripting UI](.github/scripting1.PNG)

And best of all, it's completely [free](https://www.fsf.org/about/what-is-free-software) (as in "free speech" as well as "free beer"). 

Orko is a new application which has missing features and some well-known bugs. It needs your help. [Read more](https://github.com/gruelbox/orko/wiki/Why-Orko) about Orko, [why it's free](https://github.com/gruelbox/orko/wiki/Supporting_The_Project) and [how to help](https://github.com/gruelbox/orko/wiki/Project-status).

<img align="right" src=".github/mobile1.png" width="281" height="609"/>

## Status

**Beta**. Please read the [project status page](https://github.com/gruelbox/orko/wiki/Project-status).

Particularly, please note that the application has only so far been tested on Chrome desktop and Chrome mobile.

## Quick-start Installation

- [On a local machine](https://github.com/gruelbox/orko/wiki/Local-installation)
- [One-click install on Heroku](https://github.com/gruelbox/orko/wiki/One-click-installation-on-Heroku)
- [Manual install on Heroku](https://github.com/gruelbox/orko/wiki/Manual-installation-on-Heroku)

## Help wanted

At the moment this project is a labour of love for just me, I am creating new bug and enhancement issues faster than I am closing them, and I [need help](https://github.com/gruelbox/orko/issues/111)!

The **back-end server** is written in Java and is based on [XChange](https://github.com/knowm/XChange) and [xstream-stream](https://github.com/bitrich-info/xchange-stream), to which the project is a significant contributor. Please consider helping these great projects - it has a knock-on effect on Orko.

The **front-end UI** (mobile and desktop) is written in Javascript and is based on React+Redux.

## Everything else

See [the wiki](https://github.com/gruelbox/orko/wiki)!
