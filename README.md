# Orko [![Twitter](http://i.imgur.com/wWzX9uB.png)](https://twitter.com/orkotrading)

[![CircleCI](https://circleci.com/gh/gruelbox/orko/tree/master.svg?style=svg&circle-token=3e040c3e064daf7408b29df31c61af9c73ea862a)](https://circleci.com/gh/gruelbox/orko/tree/master)
[![Cypress.io tests](https://img.shields.io/badge/cypress.io-tests-green.svg?style=flat-square)](https://dashboard.cypress.io/#/projects/ttud56/runs)
[![Travis](https://travis-ci.org/gruelbox/orko.svg?branch=master)](https://travis-ci.org/gruelbox/orko)
[![Sonarcloud Security Rating](https://sonarcloud.io/api/project_badges/measure?project=com.gruelbox%3Aorko-parent&metric=security_rating)](https://sonarcloud.io/dashboard?id=com.gruelbox%3Aorko-parent)
[![Sonarcloud Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=com.gruelbox%3Aorko-parent&metric=vulnerabilities)](https://sonarcloud.io/dashboard?id=com.gruelbox%3Aorko-parent)
[![Sonarcloud Coverage](https://sonarcloud.io/api/project_badges/measure?project=com.gruelbox%3Aorko-parent&metric=coverage)](https://sonarcloud.io/dashboard?id=com.gruelbox%3Aorko-parent)
[![Sonarcloud Status](https://sonarcloud.io/api/project_badges/measure?project=com.gruelbox%3Aorko-parent&metric=alert_status)](https://sonarcloud.io/dashboard?id=com.gruelbox%3Aorko-parent)

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

## Help wanted

At the moment this project is a labour of love for just me, I am creating new bug and enhancement issues faster than I am closing them, and I [need help](https://github.com/gruelbox/orko/issues/111)!

The **back-end server** is written in Java and is based on [XChange](https://github.com/knowm/XChange) and [xstream-stream](https://github.com/bitrich-info/xchange-stream), to which the project is a significant contributor. Please consider helping these great projects - it has a knock-on effect on Orko.

The **front-end UI** (mobile and desktop) is written in Javascript and is based on React+Redux.

## Everything else

See [the wiki](https://github.com/gruelbox/orko/wiki)!
