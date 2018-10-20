# Orko

Orko is a web application which provides a unified UI and web service API to numerous cryptocurrency exchanges, allowing you to trade and manage
your portfolio, even if it is spread across multiple exchanges, all from one screen. It supports powerful background processes allowing you
to set up stop losses, take profit orders, trailing stops and more, even on exchanges that don't support these types of advanced orders, as well
as price and profit/loss level alerts.

It is under active development and is gradually being extended to the point where you will also be able to schedule and monitor complex
scripted strategies. It aims to be a one-stop-shop for online cryptocurrency trading.

## Just let me try it out

```
// Build the server
sudo apt-get install maven
mvn clean package -Dmaven.test.skip=true

// Run the single-app server
java -cp orko-all-in-one/target/classes:orko-all-in-one/target/dependency/* com.grahamcrockford.orko.allinone.AllInOneApplication server ./example-developer-mode-config-allinone.yml

// Build the UI
cd orko-ui
sudo apt-get install npm
npm install

// Run the UI
HTTPS=true npm start
```

This should start the application in a local web browser. If it doesn't, navigate to https://localhost:3000.

Note that:

- Since it uses self-sign certificate, you may need to accept some browser warnings.
- This uses in-memory storage only. Any jobs you create will not be saved so will be lost on restart. To enable persistent storage, you need a MongoDB database (more on this below).
- It has none of your exchange details, so no balance or trade history information is available, and all trading is paper-trading.

## Add your exchange account details

By default there are no exchange account details, so trading isn't enabled. To remedy this, copy `example-developer-mode-config-allinone.yml` to a new file (e.g. `my-config.yml`), then, to the relevant section, add the API keys details for the exchanges you use.

Then run the backend pointing to this config file instead:

```
java -cp orko-all-in-one/target/classes:orko-all-in-one/target/dependency/* com.grahamcrockford.orko.allinone.AllInOneApplication server ./my-config.yml
```

## Set up Telegram so you can get notifications on your phone

While notifications are shown in the UI, it's handy to get them away from your screen.

1. Create a Telegram bot using the [BotFather](https://core.telegram.org/bots). Note down the API token.
1. Create a new channel from the Telegram app, and make it public (we'll make it private shortly).
1. Add your bot as a member of the channel, so it can post to it.
1. Use the following URL to get the ID of your channel: https://api.telegram.org/YOURBOTID:YOURTOKEN/getChat?chat_id=@YourChannelName
1. Once you've noted down the channel ID, make your channel private from the app so no-one else can access it (you can't use the above API to ge the IP of a private channel).

Once you have the connection details, you can set the appropriate section in your `my-config.yml` file. Uncomment (remove the # symbols) from the following lines, replacing the values with the token and chat id you noted down.

```
# telegram:
#   botToken: YOU
#   chatId: REALLYWANTTHIS
```

Then restart. The application will now use this bot to send you notifications on your private channel. You can add more people to the channel if you like.

## Enable persistent storage

TODO consider defaulting to an embedded mongo: https://github.com/flapdoodle-oss/de.flapdoodle.embed.mongo
TODO TO FLESH OUT WITH INSTALLATION INSTRUCTIONS

In order to be able to shut down and start up the application, or run multiple instances of the application for fault tolerance, you need a real database.

Install MongoDB.

Create a DB and set up an admin user:

`db.createUser({user: "jsmith", pwd: "some-initial-password", roles: [{role: "readWrite", db: "yourdb" }]})`

Then uncomment this section in the config file, replacing the details accordingly.

```
#database:
#  mongoClientURI: mongodb://jsmith:some-initial-password@localhost:27017/yourdb # Your mongoDB connection details.
#  lockSeconds: 10
```

## How to deploy to Heroku

Once you've got it working locally, you probably want to deploy it somewhere it's not going to fall over. I like Heroku. The Hobby account is cheap at $7/pm per server if running constantly, SSL is provided out of the box and continuous deployment is sexy as fuck.

1. Create a Heroku account
1. Using the approach detailed in the getting started guide for Java at https://devcenter.heroku.com/articles/getting-started-with-java#set-up, and create a new, empty Java application.
1. You'll need Hobby Tier, which means a credit card. It's free until you pass a certain number of minutes running per month. If you want to be a skinflint, just take it down when you're not using it.
1. Add the mLab MongoDB addon (required)
1. Add the Papertrail addon (optional, but by far the easiest way to handle logs)

Set up the environment variables in addition to those already configured by the add-ons you've provisioned:

| Variable                  | Set to                                                                                                                                                                                                                                                                            |
| ------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `LOOP_SECONDS`            | 15                                                                                                                                                                                                                                                                                |
| `LOCK_SECONDS`            | 45                                                                                                                                                                                                                                                                                |
| `PROXIED`                 | Set to `true` on Heroku so it uses the `X-Forwarded-For` header to determine the source IP. This MUST be `false` if you're not hosted behind a trusted proxy where you can 100% believe the `X-Forwarded-For` header, or someone could easily spoof their IP and bypass your 2FA. |
| `MONGODB_URI`             | Should already have been set up for you by the add-on.                                                                                                                                                                                                                            |
| `TELEGRAM_BOT_TOKEN`      | The bot API token. Can be left blank, in which case Telegram notifications won't be used, but must be defined. Note that at the moment, there are no other notifications (even on-screen) so it's a bit of a nightmare to use without phone notifiations. Turn them on.           |
| `TELEGRAM_CHAT_ID`        | The chat ID. Must be defined but may be blank if `TELEGRAM_BOT_TOKEN` is.                                                                                                                                                                                                         |
| `CRYPTOPIA_API_KEY`       | Your Cryptopia API key. May be left blank for paper trading.                                                                                                                                                                                                                      |
| `CRYPTOPIA_SECRET`        | Your Cryptopia API secret. May be left blank for paper trading.                                                                                                                                                                                                                   |
| `GDAX_SANDBOX_API_KEY`    | Your API key from the GDAX sandbox (https://public.sandbox.gdax.com). If left blank, paper trading will be used.                                                                                                                                                                  |
| `GDAX_SANDBOX_SECRET`     | Your secret from the GDAX sandbox (https://public.sandbox.gdax.com). May be left blank for paper trading.                                                                                                                                                                         |
| `GDAX_SANDBOX_PASSPHRASE` | Your passphrase from the GDAX sandbox (https://public.sandbox.gdax.com). May be left blank for paper trading.                                                                                                                                                                     |
| `GDAX_API_KEY`            | Your GDAX API key. May be left blank for paper trading.                                                                                                                                                                                                                           |
| `GDAX_SECRET`             | Your GDAX secret. May be left blank for paper trading.                                                                                                                                                                                                                            |
| `GDAX_PASSPHRASE`         | Your GDAX passphrase. May be left blank for paper trading.                                                                                                                                                                                                                        |
| `BINANCE_API_KEY`         | Your Binance API key. May be left blank for paper trading.                                                                                                                                                                                                                        |
| `BINANCE_SECRET`          | Your Binance secret. May be left blank for paper trading.                                                                                                                                                                                                                         |
| `BITFINEX_API_KEY`        | Your BitFinex API key. May be left blank for paper trading.                                                                                                                                                                                                                       |
| `BITFINEX_SECRET`         | Your Binance secret. May be left blank for paper trading.                                                                                                                                                                                                                         |
| `BITTREX_API_KEY`         | our Bittrex API key. May be left blank for paper trading.                                                                                                                                                                                                                         |
| `BITTREX_SECRET`          | our Bittrex secret. May be left blank for paper trading.                                                                                                                                                                                                                          |
| `KUCOIN_API_KEY`          | Your Kucoin API key. May be left blank for paper trading.                                                                                                                                                                                                                         |
| `KUCOIN_SECRET`           | Your Kucoin secret. May be left blank for paper trading.                                                                                                                                                                                                                          |
| `AUTH_TOKEN`              | Your 2FA secret key (generated with `java -cp target/orko-0.0.1-SNAPSHOT.jar com.grahamcrockford.orko.cli.GenerateSecretKey`) - more on this below. Can be left blank (in which case 2FA whitelisting is disabled) but must be defined. Strongly recommended to be enabled.       |
| `OKTA_BASEURL`            | Will be provided during Okta setup (see below)                                                                                                                                                                                                                                    |
| `OKTA_CLIENTID`           | Will be provided during Okta setup (see below)                                                                                                                                                                                                                                    |
| `OKTA_ISSUER`             | Will be provided during Okta setup (see below)                                                                                                                                                                                                                                    |
| `JAVA_OPTS`               | `-server -Xmx185m -Xms185m -Xss256k -XX:MaxMetaspaceSize=80m -XX:+UseG1GC -Dsun.net.inetaddr.ttl=60 -Dio.netty.leakDetectionLevel=advanced`                                                                                                                                       |
| `LOG_LEVEL`               | `INFO` (or `DEBUG` if you need it)                                                                                                                                                                                                                                                |
| `MAVEN_CUSTOM_OPTS`       | `--update-snapshots -DskipTests=true`                                                                                                                                                                                                                                             |
| `MAVEN_CUSTOM_GOALS`      | `clean package`                                                                                                                                                                                                                                                                   |

We now need to worry about security.

The application hosts REST endpoints and a single web socket. Both are protected at the servlet container level; they will return HTTP status 401 if a suitable authorization header isn't included. This takes one of two forms.

For the HTTP endpoints, the fairly standard:

```
authorization: Bearer MYJSONWEBTOKEN
```

For the Websockets, the entirely nonstandard:

```
Sec-WebSocket-Protocol: auth, MYJSONWEBTOKEBN
```

This latter is due to the fact that it's the only way to pass a JWT in the standard Javascript `WebSocket` constructor:

```
new WebSocket('wss://localhost:8080/ws', ['auth', 'MYJSONWEBTOKEBN'])
```

Call me paranoid, but I didn't like the idea of an attacker being able to open a websocket at all without authentication (which is a protocol I have complete control over and which means I need to trust my own state management code to be sure an attacker can't do stuff) - I preferred the idea of stopping them at the level of a much more restrictive protocol where I can just trust Java servlet filters to do their job.

Mostly you don't need to worry about this, because it's delegated to Okta - more on this in a moment.

The other element is that we will return 402 if the origin IP address isn't on a whitelist. To avoid this being too restrictive, we provide a single REST entry point (`/auth`) which you can use to whitelist your IP address at any time, by passing a valid Google authenticator code. Only a single IP address can be whitelisted at any one time and whitelisting expires.

So, first, let's create a 2FA key:

1. Generate a new 2FA secret using `java -cp orko-web/target/orko-web.jar com.grahamcrockford.orko.web.cli.GenerateSecretKey`
1. Store that somewhere safe and enter it into Google Authenticator on your phone.
1. If you don't want to set up a Java environment, give me a shout and I'll generate a keypair for you.
1. Set the `AUTH_TOKEN` environment variable accordingly.

Now you need an Okta account to handle the JWT authentication:

1. Create a basic (free) account at https://www.okta.com/
1. Add any 2FA or whatever you feel appropriate to this account.
1. Create new application of type Single Page App (SPA), allowing both ID token and Access Token
1. Set your Login redirect URI and Initiate login URI to the address of your front-end server.
1. Note down the client ID and set it in your backend app's environment variables.
1. Go to the Sign On tab and note the Issuer. Set the `OKTA_BASEURL` and `OKTA_ISSUER` variables to this. For the `OKTA_ISSUER` variable, append it with `/oauth2/default`.

Now you're ready to deploy.

1. You should have already installed Heroku CLI.
1. From a clean checkout of this code, add the heroku remote:

```
git remote add heroku git@heroku.com:your-app-name.git
```

1. Then simply push to the heroku remote.

That's it!
