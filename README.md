# Orko

[![CircleCI](https://circleci.com/gh/badgerwithagun/orko/tree/master.svg?style=svg&circle-token=3e040c3e064daf7408b29df31c61af9c73ea862a)](https://circleci.com/gh/badgerwithagun/orko/tree/master)

[![Deploy](https://www.herokucdn.com/deploy/button.svg)](https://heroku.com/deploy?template=https://github.com/badgerwithagun/orko)

Orko is a web application which provides a unified UI and web service API to numerous cryptocurrency exchanges, allowing you to trade and manage your portfolio, even if it is spread across multiple exchanges, all from one screen. It supports powerful background processes allowing you to set up stop losses, take profit orders, trailing stops and more, even on exchanges that don't support these types of advanced orders, as well as price and profit/loss level alerts.

## But why another solution?

Orko is designed to solve a range of problems with existing solutions:

- Cryptocurrency exchanges' own features often leave a lot to be desired. Some tend to be missing important features such as stop losses or trailing stops, others are simply buggy, hard to use or suffer extreme lag during busy trading (with one or two notable exceptions).
- Many advanced trading techniques are often only available if you have the technical capability to write "bots" or scripts to access exchanges' APIs. While these are deceptively simple to write initially, it's extremely hard to write something reliable and stable.
- Traders who use multiple exchanges have log into them all separately. This can be onerous, particularly given that they have varying, and usually aggressive, security measures which take some time to get past, and even worse if a trader uses multiple devices. It also means that it is often hard to get a "single version of the truth".
- Existing solutions to these problems, for reasons of easy monetisation, are usually commercial, "multi-tenant" applications, where a trader pays for access to a shared server. There are two problems here:
  - The trader's API keys are potentially vulnerable to hacks by other users. There have been several instances where exchanges have blamed users who use shared trading applications for losses as a result of not properly securing their keys (that is, uploading them to a third party service).
  - Such services often get blocked by exchanges, due to having large numbers of users "spamming" exchange APIs from a small number of IP addresses. These can make it impossible to trade for extended periods.

## What Orko does differently

- Orko is a **strictly single-tenant** application. That is, each instance of the application services one, and only one user. You manage that application - the developers of Orko have no access to it. That means your API keys are never shared. You can run it on your own hardware in your own home or trust it to a cloud provider account to which only you have access.
- Orko's code is open source, so it's hard for anyone to add exploit code which could put you at risk. If you're really paranoid, you can fork the code to make sure it doesn't change, then audit it line-by-line. You don;t have to trust your valuable API keys to unknown code.
- You can change it and improve it (and we'd be really grateful if you shared it with the rest of the community by submitting a [pull request](https://github.com/badgerwithagun/orko/pulls)).
- It's really easy to deploy and run. If you have a [Heroku](https://heroku.com/) account, You can deploy it [with just a few clicks](https://heroku.com/deploy?template=https://github.com/badgerwithagun/orko), or download it and run it in just a few commands.
- It's being constantly improved and extended. What you have here is a very early version, with only a fraction of the features we're intending to add, but it has been used as our main trading UI for over a year as it has been developed.

## Status

ALPHA. This means specifically:

- I trust you a great deal. <3
- No actual releases. You must check out the code and build yourself (instructions below)
- All builds are snapshots with version 0.0.1-SNAPSHOT. Don't expect version numbers to tell you anything.
- No compatibility is guaranteed between local database versions (you may need to delete local database files between upgrades), although compatibility _is_ guaranteed with Mongo databases, so live installations online will be safe between versions
- Features may appear/disappear or get changed or combined without warning.
- The UI and web APIs may change drastically without warning
- All this means there's limited automated testing. Expect occasional regressions.

## Quick start (local install)

```
sudo apt-get install maven
./build.sh
./start.sh
```

Navigate to http://localhost:8080 to view the application.

Note that:

- This uses local files (in the current directory) to hold state. It's not hugely robust and doesn't support multiple instances. For a production deployment, a standalone database is recommended. For more information on this, see below.
- Without your exchange details, no balance or trade history information is available, and all trading is paper-trading. We'll add these in a moment.
- There's no out-of-the-box support for SSL. All details are sent in the clear, so don't deploy this anywhere public. To make it secure to deploy, either wrap it in an Apache/nginx proxy or publish to a turnkey platform like Heroku (more on this below).
- Authentication features are all disabled. We talk through enabling these in the Heroku setup instructions below.

## Add your exchange account details

By default there are no exchange account details, so trading isn't enabled. To remedy this, modify `orko-all-in-one/example-config.yml`. To the relevant sections, add the API keys details for the exchanges you use. Leave any exchanges you don't have API details for blank. Then run again.

## Set up Telegram so you can get notifications on your phone

While notifications are shown in the UI, it's handy to get them away from your screen.

1. Create a Telegram bot using the [BotFather](https://core.telegram.org/bots). Note down the API token.
1. Create a new channel from the Telegram app, and make it public (we'll make it private shortly).
1. Add your bot as a member of the channel, so it can post to it.
1. Use the following URL to get the ID of your channel: https://api.telegram.org/YOURBOTID:YOURTOKEN/getChat?chat_id=@YourChannelName
1. Once you've noted down the channel ID, make your channel private from the app so no-one else can access it (you can't use the above API to ge the IP of a private channel).

Once you have the connection details, you can set the appropriate section in your `example-config.yml` file. Uncomment (remove the # symbols) from the following lines, replacing the values with the token and chat id you noted down.

```
# telegram:
#   botToken: YOU
#   chatId: REALLYWANTTHIS
```

Then restart. The application will now use this bot to send you notifications on your private channel. You can add more people to the channel if you like.

## Use a standalone MongoDB database

MongoDB is a much more reliable database engine and recommended if you're going to run the application for real trading. To set it up:

1. Install MongoDB.
2. Create a DB and set up an admin user:
   `db.createUser({user: "jsmith", pwd: "some-initial-password", roles: [{role: "readWrite", db: "yourdb" }]})`
3. Update the relevant section in your config file:

```
database:
# mongoDbFileDir: COMMENT THIS OUT
  mongoClientURI: mongodb://jsmith:some-initial-password@localhost:27017/yourdb # Your mongoDB connection details.
  lockSeconds: 10
```

## How to deploy to Heroku

### Introduction

I personally run the application on Heroku. The Hobby account is cheap at $7/pm per server if running constantly, SSL is provided out of the box and the continuous deployment features are great.

Best of all, the application is all preconfigured to work out of the box.

### Create a 2FA key

Security is (optionally) double-layered:

- The first layer is a conventional login/JWT. We're going to set this up in the simplest possible way, but you can optionally delegate this to Okta.
- The second layer is a dynamic IP whitelisting. The UI must supply a valid Google Authenticator code to the backend which will whitelist the originating IP for a fixed period. All other entry points will return an HTTP 402 until this is done.

Generating the key is done offline for additional security.

1. Generate a new 2FA secret using `./generate-key.sh` (you need to have done a build first using `./build.sh`).
1. Note it down - we'll need it when configuring the application.
1. Enter it into Google Authenticator on your phone.

### Get set up on Heroku

Note that the following settings will result in an application running on the Heroku Free Tier, which will shut down your application after a few minutes of inactivity. This is OK for a demo, but totally unsuitable for running in real life, since monitoring the market in the background is the whole point! If you want to use the application. you'll need Hobby Tier, which means a credit card. If you want to be a skinflint, just take it down when you're not using it.

Let's get started. Create a Heroku account and [install Heroku CLI locally](https://devcenter.heroku.com/articles/heroku-cli).

Now, from the the directory where you've cloned this repository (the same directly as this README):

```
> heroku create
> git push heroku master
```

Add the following addons: **Papertrail** and **M-labs MongoDB**.

Set up the following environment variables in addition to those already configured by the add-ons you've provisioned:

| Variable                           | Set to                                                                                                                                                 |
| ---------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------ |
| `TELEGRAM_BOT_TOKEN`               | The Telegram bot API token. Can be omitted, in which case Telegram notifications won't be used.                                                        |
| `TELEGRAM_CHAT_ID`                 | The Telegram chat ID. May be omitted if `TELEGRAM_BOT_TOKEN` is.                                                                                       |
| `AUTH_TOKEN`                       | Your 2FA secret key used when whitelisting an IP. Can be omitted if you don't want this additional layer of security.                                  |
| `SIMPLE_AUTH_USERNAME`             | The username you want to use when logging in.                                                                                                          |
| `SIMPLE_AUTH_PASSWORD`             | The password you want to use when logging in.                                                                                                          |
| `SIMPLE_AUTH_SECRET`               | A long, randomised string of characters to act as the cryptocraphic seed for issued tokens.                                                            |
| `SIMPLE_AUTH_TOKEN_EXPIRY_MINUTES` | The time before each token issued will expire and the user will be forced to log in again, in minutes. 1440 is a sensible default.                     |
| `SIMPLE_AUTH_SECOND_FACTOR`        | A 2FA secret key used when logging in. Can either be same value as AUTH_TOKEN, or if you want to be super-secure, use a completely different 2FA code. |

Optionally, you can add any of these to add authenticated support for exchanges where you have API keys:

| Variable            | Set to                     |
| ------------------- | -------------------------- |
| `CRYPTOPIA_API_KEY` | Your Cryptopia API key.    |
| `CRYPTOPIA_SECRET`  | Your Cryptopia API secret. |
| `GDAX_API_KEY`      | Your GDAX API key.         |
| `GDAX_SECRET`       | Your GDAX secret.          |
| `GDAX_PASSPHRASE`   | Your GDAX passphrase.      |
| `BINANCE_API_KEY`   | Your Binance API key.      |
| `BINANCE_SECRET`    | Your Binance secret.       |
| `BITFINEX_API_KEY`  | Your BitFinex API key.     |
| `BITFINEX_SECRET`   | Your Binance secret.       |
| `BITTREX_API_KEY`   | Your Bittrex API key.      |
| `BITTREX_SECRET`    | Your Bittrex secret.       |
| `KUCOIN_API_KEY`    | Your Kucoin API key.       |
| `KUCOIN_SECRET`     | Your Kucoin secret.        |

The following are defaulted or automatically set up, so you can ignore them unless you need to change the defaults:

| Variable                   | Default                                                                                                                                     |
| -------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------- |
| `LOOP_SECONDS`             | 15                                                                                                                                          |
| `LOCK_SECONDS`             | 45                                                                                                                                          |
| `MONGODB_URI`              | Should already have been set up for you by the MongoDB add-on.                                                                              |
| `WHITELIST_EXPIRY_SECONDS` | How long an IP whitelisting should last. 86400 (24 hours) is a good default.                                                                |
| `JAVA_OPTS`                | `-server -Xmx185m -Xms185m -Xss256k -XX:MaxMetaspaceSize=80m -XX:+UseG1GC -Dsun.net.inetaddr.ttl=60 -Dio.netty.leakDetectionLevel=advanced` |
| `LOG_LEVEL`                | `INFO` (or `DEBUG` if you need it)                                                                                                          |
| `MAVEN_CUSTOM_OPTS`        | `-Pproduction --update-snapshots -DskipTests=true -T 1C`                                                                                    |
| `MAVEN_CUSTOM_GOALS`       | `clean package`                                                                                                                             |

That's it! Visit https://your-app-name.herokuapp.com to go through secuity and log in.

### Delegate your security

If you don't trust home-grown JWT issuance or storing credentials on Heroku, you can optionally delegate authentication to Okta:

1. Create a basic (free) account at https://www.okta.com/
1. Add any 2FA or whatever you feel appropriate to this account.
1. Create new application of type Single Page App (SPA), allowing both ID token and Access Token
1. Set your Login redirect URI and Initiate login URI to the address of your front-end server.
1. Note down the client ID and set it in your backend app's environment variables.
1. Go to the Sign On tab and note the `Issuer` and `Client id`.
1. Now change your Heroku environment variables as follows:

| Variable                           | Set to                                                                                   |
| ---------------------------------- | ---------------------------------------------------------------------------------------- |
| `AUTH_TOKEN`                       | Your 2FA secret key. Can be omitted if you don't want this additional layer of security. |
| `OKTA_BASEURL`                     | The Okta issuer.                                                                         |
| `OKTA_CLIENTID`                    | The Okta client ID.                                                                      |
| `OKTA_ISSUER`                      | The Okta issuer appended with `/oauth2/default`                                          |
| `SIMPLE_AUTH_USERNAME`             | REMOVE THIS                                                                              |
| `SIMPLE_AUTH_PASSWORD`             | REMOVE THIS                                                                              |
| `SIMPLE_AUTH_SECRET`               | REMOVE THIS                                                                              |
| `SIMPLE_AUTH_TOKEN_EXPIRY_MINUTES` | REMOVE THIS                                                                              |
