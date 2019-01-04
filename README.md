# Orko [![Twitter](http://i.imgur.com/wWzX9uB.png)](https://twitter.com/orkotrading)

[![CircleCI](https://circleci.com/gh/badgerwithagun/orko/tree/master.svg?style=svg&circle-token=3e040c3e064daf7408b29df31c61af9c73ea862a)](https://circleci.com/gh/badgerwithagun/orko/tree/master)
[![Cypress.io tests](https://img.shields.io/badge/cypress.io-tests-green.svg?style=flat-square)](https://cypress.io)

[![Deploy to Heroku](https://www.herokucdn.com/deploy/button.svg)](https://heroku.com/deploy?template=https://github.com/badgerwithagun/orko)

Are you up of logging into multiple separate cryptocurrency exchanges to trade and track your portfolio? Frustrated at the different feature levels exchanges provide? Wanting to automate your trading with advanced features such as trailing stops, one-cancels-other, soft stops which ignore sharp wicks?

Orko is a web application which provides a unified dashboard to control numerous cryptocurrency exchanges, allowing you to trade and manage your portfolio, even if it is spread across multiple exchanges, all from one screen, and without sharing your API keys with anyone. [Read more...](https://github.com/badgerwithagun/orko/wiki/Why-Orko).

## Status

**Beta**.

The application is in active daily use by me (I built it for myself after all), but has only just been made available to the public.  As it started off as a personal project, there are [quite a lot](https://github.com/badgerwithagun/orko/issues) of known issues, but a [clear roadmap](https://github.com/badgerwithagun/orko/projects) to getting those resolved.

**If you are a user**, please use with caution and report any [issues you find](https://github.com/badgerwithagun/orko/issues).

**If you are a developer**, I really need Java and Javascript/React developers to help taking the project to the next level, so please get in contact if you want to help.

[Read more...](https://github.com/badgerwithagun/orko/wiki/Project-status)

## User Guide

Usage and setup instructions are under construction on [the wiki](https://github.com/badgerwithagun/orko/wiki).

## Quick start (local install)

Clone this repository, then from the root directory:

```
sudo apt-get install maven
./build.sh
./start.sh
```

Navigate to http://localhost:8080 to view the application.

Note that this mode has numerous limitations. You need to do some more setup to get the full experience. [Read more...](https://github.com/badgerwithagun/orko/wiki/Local-installation)

## Deploy on Heroku

Okta is preconfigured and optimised to run securely on [Heroku](https://www.heroku.com/). The Hobby account is cheap at \$7/pm per server if running constantly. To instantly deploy your own instance of the application, just click the button below.

[![Deploy to Heroku](https://www.herokucdn.com/deploy/button.svg)](https://heroku.com/deploy?template=https://github.com/badgerwithagun/orko)

If that doesn't work, or you don't trust it, you can [install the application manually](https://github.com/badgerwithagun/orko/wiki/Manual-installation-on-Heroku).

## Donations

Orko was created and is maintained free of charge for you to use. If it helps you, please consider a donation to fund the project.

| Ticker | Name     | Address                                                          |
| ------ | -------- | ---------------------------------------------------------------- |
| BTC    | Bitcoin  | bc1qvuhym7rux57ctx2z4fsyg4lzz3gyj0l3mwge9r (SEGWIT ONLY)         |
| ETH    | Ethereum | TBC                                                              |
| NANO   | Nano     | xrb_3ix1cmsbpsgjbxq3bx9xt1x7ezhe8u9pqtkq5bnpmbjtw5nymf5ph5duxfpg |
